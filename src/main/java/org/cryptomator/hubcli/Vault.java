package org.cryptomator.hubcli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(name = "vault",
		description = "Manage vaults.",
		subcommands = {VaultCreate.class, VaultUpdate.class, VaultRecoveryKey.class, VaultAddUser.class, VaultAddGroup.class, VaultRemoveMember.class, VaultCreateTemplate.class})
class Vault {

	private static final Logger LOG = LoggerFactory.getLogger(Vault.class);

	@CommandLine.Mixin
	Common common;

	@CommandLine.Mixin
	AccessToken accessToken;

	@CommandLine.Command(name = "list", description = "List owned vaults.")
	public Integer list() throws InterruptedException, IOException {
		try (var backend = new Backend(accessToken.value, common.getApiBase())) {
			var accessible = backend.getVaultService().listOwned().body();
			System.out.println(accessible);
			return 0;
		} catch (UnexpectedStatusCodeException e) {
			LOG.error(e.getMessage(), e);
			return e.asExitCode();
		}
	}
}
