package org.cryptomator.hubcli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.BaseEncoding;
import com.nimbusds.jose.JWEObject;
import org.cryptomator.cryptolib.common.P384KeyPair;
import org.cryptomator.hubcli.model.VaultRole;
import org.cryptomator.hubcli.util.JWEHelper;
import org.cryptomator.hubcli.util.KeyHelper;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.Callable;

@Command(name = "add-user", description = "Add a user to a vault")
public class AddVaultUser implements Callable<Integer> {

	@Mixin
	Common common;

	@Mixin
	AccessToken accessToken;

	@Mixin
	P12 p12;

	@Option(names = {"--vault-id"}, required = true, description = "id of the vault")
	String vaultId;

	@Option(names = {"--user-id"}, required = true, description = "id of an user")
	String userId;

	@Option(names = {"--role"}, description = "role of the user (${COMPLETION-CANDIDATES})", defaultValue = "MEMBER")
	VaultRole vaultRole;

	@Override
	public Integer call() throws ParseException, GeneralSecurityException, InterruptedException, IOException {
		// parse access token:
		var jwt = accessToken.parsed();
		if (jwt.getJWTClaimsSet().getExpirationTime().toInstant().isBefore(Instant.now())) {
			throw new IllegalArgumentException("Access token expired");
		}

		// read p12:
		var deviceKeyPair = P384KeyPair.load(p12.file, p12.password);
		var deviceId = KeyHelper.getKeyId(deviceKeyPair.getPublic());


		try (var httpClient = HttpClient.newHttpClient()) {
			// get member info
			var memberInfoReq = createRequest("authorities?ids=" + userId).GET().build();
			var memberInfoRes = sendRequest(httpClient, memberInfoReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8), 200);
			var memberPublicKeyNode = new ObjectMapper().reader().readTree(memberInfoRes.body()).get(0).get("publicKey"); // FIXME: NPE if user list is empty
			var memberPublicKeyStr = memberPublicKeyNode.isNull() ? null : memberPublicKeyNode.asText();
			if (memberPublicKeyStr == null) {
				System.err.println("User not set up.");
				return 1;
			}

			var memberPublicKeyBytes = BaseEncoding.base64().decode(memberPublicKeyStr);
			var memberPublicKey = KeyHelper.readX509EncodedEcPublicKey(memberPublicKeyBytes);

			// get vault key
			var vaultKeyReq = createRequest("vaults/" + vaultId + "/access-token").GET().build();
			var vaultKeyRes = sendRequest(httpClient, vaultKeyReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.US_ASCII), 200);
			var vaultKeyJWE = vaultKeyRes.body();

			// get device info
			var deviceReq = createRequest("devices/" + deviceId).GET().build();
			var deviceRes = sendRequest(httpClient, deviceReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8), 200);
			var cliUserPrivateKeyJWE = new ObjectMapper().reader().readTree(deviceRes.body()).get("userPrivateKey").asText();

			// crypto
			var cliUserPrivateKey = JWEHelper.decryptUserKey(JWEObject.parse(cliUserPrivateKeyJWE), deviceKeyPair.getPrivate());
			String memberSpecificVaultKey;
			try (var vaultKey = JWEHelper.decryptVaultKey(JWEObject.parse(vaultKeyJWE), cliUserPrivateKey)) {
				memberSpecificVaultKey = JWEHelper.encryptVaultKey(vaultKey, memberPublicKey).serialize();
			}

			// add user
			var addUserReq = createRequest("vaults/" + vaultId + "/users/" + userId + "?role=" + vaultRole.name())
					.PUT(HttpRequest.BodyPublishers.noBody())
					.build();
			sendRequest(httpClient, addUserReq, HttpResponse.BodyHandlers.discarding(), 200, 201);

			// grant access
			var grantAccessReq = createRequest("vaults/" + vaultId + "/access-tokens/" + userId)
					.PUT(HttpRequest.BodyPublishers.ofString(memberSpecificVaultKey))
					.header("Content-Type", "text/plain")
					.build();
			sendRequest(httpClient, grantAccessReq, HttpResponse.BodyHandlers.discarding(), 201);
			return 0;
		} catch (UnexpectedStatusCodeException e) {
			return e.status;
		}
	}

	private HttpRequest.Builder createRequest(String path) {
		var uri = common.getApiBase().resolve(path);
		return HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(5)).header("Authorization", "Bearer " + accessToken.value);
	}

	private <T> HttpResponse<T> sendRequest(HttpClient httpClient, HttpRequest request, HttpResponse.BodyHandler<T> bodyHandler, int... expectedStatusCode) throws IOException, InterruptedException, UnexpectedStatusCodeException {
		var res = httpClient.send(request, bodyHandler);
		var status = res.statusCode();
		if (Arrays.stream(expectedStatusCode).noneMatch(s -> s == status)) {
			throw new UnexpectedStatusCodeException(status, "Unexpected response for " + request.method() + " " + request.uri() + ": " + status);
		}
		return res;
	}

}
