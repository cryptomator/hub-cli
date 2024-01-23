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

import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Callable;

@Command(name = "add-group", description = "Add a group to a vault")
class VaultAddGroup implements Callable<Integer> {

	private static final Logger LOG = LoggerFactory.getLogger(VaultAddGroup.class);

	@CommandLine.ParentCommand
	Vault parentCmd;

	@Mixin
	P12 p12;

	@Option(names = {"--vault-id", "-v"}, required = true, description = "id of the vault")
	UUID vaultId;

	@Option(names = {"--group-id", "-g"}, required = true, description = "id of a group")
	String groupId;

	@Option(names = {"--role"}, description = "role of the user (${COMPLETION-CANDIDATES})", defaultValue = "MEMBER")
	VaultRole vaultRole;

	@Override
	public Integer call() throws Exception {
		// parse access token:
		var jwt = parentCmd.accessToken.parsed();
		if (jwt.getJWTClaimsSet().getExpirationTime().toInstant().isBefore(Instant.now())) {
			throw new IllegalArgumentException("Access token expired");
		}

		// read p12:
		var deviceKeyPair = P384KeyPair.load(p12.file, p12.password);
		var deviceId = KeyHelper.getKeyId(deviceKeyPair.getPublic());

		try (var backend = new Backend(parentCmd.accessToken.value, parentCmd.common.getApiBase())) {
			// get vault key
			var vaultKeyJWE = backend.getVaultService().getAccessToken(vaultId).body();

			// get device info
			var device = backend.getDeviceService().get(deviceId);

			// crypto
			var cliUserPrivateKey = JWEHelper.decryptUserKey(JWEObject.parse(device.userPrivateKey()), deviceKeyPair.getPrivate());

			// get all effective members including of subgroups
			var effectiveMembers = backend.getGroupService().getEffectiveMembers(groupId).stream().filter(m -> {
				if (m.publicKey() != null) {
					return true;
				} else {
					System.out.printf("Info: User %s is not set up, access cannot be granted.%n", m.name());
					return false;
				}
			}).toList();

			try (var vaultKey = JWEHelper.decryptVaultKey(JWEObject.parse(vaultKeyJWE), cliUserPrivateKey)) {
				var memberAccessTokens = new HashMap<String, String>();

				// get member info
				for (var member : effectiveMembers) {
					var memberPublicKeyBytes = BaseEncoding.base64().decode(member.publicKey());
					var memberPublicKey = KeyHelper.readX509EncodedEcPublicKey(memberPublicKeyBytes);

					String memberSpecificAccessToken = JWEHelper.encryptVaultKey(vaultKey, memberPublicKey).serialize();
					memberAccessTokens.put(member.id(), memberSpecificAccessToken);
				}

				// add group
				backend.getVaultService().addGroup(vaultId, groupId, vaultRole);

				// grant access after for loop
				if (!memberAccessTokens.isNotEmpty()) {
					backend.getVaultService().grantAccess(vaultId, memberAccessTokens);
				}
				return 0;
			}
		} catch (UnexpectedStatusCodeException e) {
			LOG.error(e.getMessage(), e);
			return e.asExitCode();
		}
	}
}
