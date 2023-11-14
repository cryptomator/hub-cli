package org.cryptomator.hubcli;

import com.google.common.base.Preconditions;
import com.google.common.hash.Hashing;
import com.nimbusds.jose.JWEObject;
import org.cryptomator.cryptolib.common.P384KeyPair;
import org.cryptomator.hubcli.util.JWEHelper;
import org.cryptomator.hubcli.util.KeyHelper;
import org.cryptomator.hubcli.util.WordEncoder;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.Callable;

@Command(name = "get-recoverykey",//
		description = "Prints the recovery key of a vault to stdout")
class GetRecoveryKey implements Callable<Integer> {

	@Mixin
	Common common;

	@Mixin
	AccessToken accessToken;

	@Mixin
	P12 p12;

	@Option(names = {"--vault-id"}, required = true, description = "id of the vault")
	String vaultId;

	@Override
	public Integer call() throws Exception {
		var deviceKeyPair = P384KeyPair.load(p12.file, p12.password);
		var deviceId = KeyHelper.getKeyId(deviceKeyPair.getPublic());
		try (var backend = new Backend(accessToken.value, common.getApiBase())) {
			// get vault key
			var vaultKeyJWE = backend.getVaultService().getAccessToken(UUID.fromString(vaultId)).body();

			// get device info
			var device = backend.getDeviceService().get(deviceId);

			// crypto
			var cliUserPrivateKey = JWEHelper.decryptUserKey(JWEObject.parse(device.userPrivateKey()), deviceKeyPair.getPrivate());
			try (var vaultKey = JWEHelper.decryptVaultKey(JWEObject.parse(vaultKeyJWE), cliUserPrivateKey)) {
				var recoveryKey = createRecoveryKey(vaultKey.getEncoded());
				System.out.println(recoveryKey);
			}
		} catch (UnexpectedStatusCodeException e) {
			System.err.println(e.getMessage());
			return e.status;
		}
		return 0;
	}

	String createRecoveryKey(byte[] rawKey) {
		Preconditions.checkArgument(rawKey.length == 64, "key should be 64 bytes");
		byte[] paddedKey = Arrays.copyOf(rawKey, 66);
		try {
			// copy 16 most significant bits of CRC32(rawKey) to the end of paddedKey:
			Hashing.crc32().hashBytes(rawKey).writeBytesTo(paddedKey, 64, 2);
			return new WordEncoder().encodePadded(paddedKey);
		} finally {
			Arrays.fill(paddedKey, (byte) 0x00);
		}
	}
}
