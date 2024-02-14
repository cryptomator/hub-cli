package org.cryptomator.hubcli;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEObject;
import org.cryptomator.cryptolib.common.P384KeyPair;
import org.cryptomator.hubcli.util.JWEHelper;
import org.cryptomator.hubcli.util.KeyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.UUID;
import java.util.concurrent.Callable;

@Command(name = "template", description = "Create vault template of existing vault")
class VaultTemplate implements Callable<Integer> {

	private static final Logger LOG = LoggerFactory.getLogger(VaultTemplate.class);

	@CommandLine.ParentCommand
	Vault parentCmd;

	@CommandLine.Mixin
	P12 p12;

	@Option(names = {"--vault-id", "-v"}, required = true, description = "id of the vault")
	UUID vaultId;

	@Option(names = {"--path"}, required = true, description = "path where to create the vault (folder) ")
	Path path;

	@Override
	public Integer call() throws IOException, InterruptedException, GeneralSecurityException, JOSEException, ParseException {
		var deviceKeyPair = P384KeyPair.load(p12.file, p12.password);
		var deviceId = KeyHelper.getKeyId(deviceKeyPair.getPublic());
		try (var backend = new Backend(parentCmd.accessToken.value, parentCmd.common.getApiBase())) {
			// get vault name
			var vaultName = backend.getVaultService().get(vaultId).name();

			// get vault key
			var vaultKeyJWE = backend.getVaultService().getAccessToken(vaultId).body();

			// get device info
			var device = backend.getDeviceService().get(deviceId);

			// crypto
			var csprng = SecureRandom.getInstanceStrong();
			var cliUserPrivateKey = JWEHelper.decryptUserKey(JWEObject.parse(device.userPrivateKey()), deviceKeyPair.getPrivate());
			try (var vaultKey = JWEHelper.decryptVaultKey(JWEObject.parse(vaultKeyJWE), cliUserPrivateKey)) {
				try (var configKeyCopy = vaultKey.copy(); var localVaulKeyCopy = vaultKey.copy()) {
					var vaultConfigString = VaultConfig.createVaultConfig(vaultId, configKeyCopy, parentCmd.common);
					VaultConfig.createLocalVault(localVaulKeyCopy, csprng, vaultConfigString, path, vaultName);
				}
			}
		} catch (UnexpectedStatusCodeException e) {
			LOG.error(e.getMessage(), e);
			return e.asExitCode();
		}
		return 0;
	}

}
