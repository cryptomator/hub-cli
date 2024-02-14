package org.cryptomator.hubcli;

import com.google.common.io.BaseEncoding;
import com.nimbusds.jose.JOSEException;
import org.cryptomator.cryptolib.api.Masterkey;
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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

@Command(name = "create", description = "Create a new vault")
class VaultCreate implements Callable<Integer> {

	private static final Logger LOG = LoggerFactory.getLogger(VaultCreate.class);

	@CommandLine.ParentCommand
	Vault parentCmd;

	@Option(names = {"--name"}, required = true, description = "name of the vault")
	String name;

	@Option(names = {"--description"}, description = "description of the vault")
	String description;

	@Option(names = {"--path"}, required = true, description = "path where to create the vault (folder) ")
	Path path;

	@Override
	public Integer call() throws IOException, InterruptedException, GeneralSecurityException, JOSEException {
		final var vaultId = UUID.randomUUID();
		var csprng = SecureRandom.getInstanceStrong();
		try (var backend = new Backend(parentCmd.accessToken.value, parentCmd.common.getApiBase()); var masterkey = Masterkey.generate(csprng)) {
			var user = backend.getUserService().getMe(false);
			if (user.publicKey() == null) {
				throw new SetupRequiredStatusCodeException();
			}
			var userPublicKeyBytes = BaseEncoding.base64().decode(user.publicKey());
			var userPublicKey = KeyHelper.readX509EncodedEcPublicKey(userPublicKeyBytes);

			try (var configKeyCopy = masterkey.copy(); var jweKeyCopy = masterkey.copy(); var localVaulKeyCopy = masterkey.copy()) {
				var vaultConfigString = VaultConfig.createVaultConfig(vaultId, configKeyCopy, parentCmd.common);
				var jwe = JWEHelper.encryptVaultKey(jweKeyCopy, userPublicKey);
				backend.getVaultService().createOrUpdateVault(vaultId, name, description, false);
				backend.getVaultService().grantAccess(vaultId, Map.of(user.id(), jwe.serialize()));
				VaultConfig.createLocalVault(localVaulKeyCopy, csprng, vaultConfigString, path, name);
			}
		} catch (UnexpectedStatusCodeException e) {
			LOG.error(e.getMessage(), e);
			return e.asExitCode();
		}
		System.out.println(vaultId);
		return 0;
	}

}
