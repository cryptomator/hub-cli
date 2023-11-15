package org.cryptomator.hubcli;

import com.google.common.io.BaseEncoding;
import com.nimbusds.jose.JWEObject;
import org.cryptomator.cryptolib.common.P384KeyPair;
import org.cryptomator.hubcli.model.VaultRole;
import org.cryptomator.hubcli.util.JWEHelper;
import org.cryptomator.hubcli.util.KeyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

@Command(name = "add-user", description = "Add a user to a vault")
public class VaultAddUser implements Callable<Integer> {

	private static final Logger LOG = LoggerFactory.getLogger(VaultAddUser.class);

	@CommandLine.ParentCommand
	Vault parentCmd;

	@Mixin
	P12 p12;

	@Option(names = {"--vault-id", "-v"}, required = true, description = "id of the vault")
	UUID vaultId;

	@Option(names = {"--user-id", "-u"}, required = true, description = "id of an user")
	String userId;

	@Option(names = {"--role"}, description = "role of the group (${COMPLETION-CANDIDATES})", defaultValue = "MEMBER")
	VaultRole vaultRole;

	@Override
	public Integer call() throws ParseException, GeneralSecurityException, InterruptedException, IOException {
		// parse access token:
		var jwt = parentCmd.accessToken.parsed();
		if (jwt.getJWTClaimsSet().getExpirationTime().toInstant().isBefore(Instant.now())) {
			throw new IllegalArgumentException("Access token expired");
		}

		// read p12:
		var deviceKeyPair = P384KeyPair.load(p12.file, p12.password);
		var deviceId = KeyHelper.getKeyId(deviceKeyPair.getPublic());

		try (var backend = new Backend(parentCmd.accessToken.value, parentCmd.common.getApiBase())) {

			// get member info
			var memberInfo = backend.getAuthorityService().listSome(List.of(userId)).getFirst(); // FIXME handle NoSuchElementException?
			if (memberInfo.publicKey() == null) {
				LOG.error("User not set up.");
				return 1;
			}
			var memberPublicKeyBytes = BaseEncoding.base64().decode(memberInfo.publicKey());
			var memberPublicKey = KeyHelper.readX509EncodedEcPublicKey(memberPublicKeyBytes);

			// get vault key
			var vaultKeyJWE = backend.getVaultService().getAccessToken(vaultId).body();

			// get device info
			var device = backend.getDeviceService().get(deviceId);

			// crypto
			var cliUserPrivateKey = JWEHelper.decryptUserKey(JWEObject.parse(device.userPrivateKey()), deviceKeyPair.getPrivate());
			String memberSpecificVaultKey;
			try (var vaultKey = JWEHelper.decryptVaultKey(JWEObject.parse(vaultKeyJWE), cliUserPrivateKey)) {
				memberSpecificVaultKey = JWEHelper.encryptVaultKey(vaultKey, memberPublicKey).serialize();
			}

			// add user
			backend.getVaultService().addUser(vaultId, userId, vaultRole);

			// grant access
			backend.getVaultService().grantAccess(vaultId, Map.of(userId, memberSpecificVaultKey));
			return 0;
		} catch (UnexpectedStatusCodeException e) {
			LOG.error(e.getMessage(), e);
			return e.status;
		}
	}

}
