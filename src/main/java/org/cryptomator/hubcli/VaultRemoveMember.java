package org.cryptomator.hubcli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Callable;

@Command(name = "remove-member", aliases = {"remove-user", "remove-group"}, description = "Remove a user or group from a vault")
class VaultRemoveMember implements Callable<Integer> {

	private static final Logger LOG = LoggerFactory.getLogger(VaultRemoveMember.class);

	@CommandLine.ParentCommand
	Vault parentCmd;

	@Option(names = {"--vault-id", "-v"}, required = true, description = "id of the vault")
	UUID vaultId;

	@Option(names = {"--member-id", "--user-id", "--group-id", "-u", "-g"}, required = true, description = "id of the user/group")
	String authorityId;

	@Override
	public Integer call() throws InterruptedException, IOException {
		try (var backend = new Backend(parentCmd.accessToken.value, parentCmd.common.getApiBase())) {
			backend.getVaultService().removeAuthority(vaultId, authorityId);
			return 0;
		} catch (UnexpectedStatusCodeException e) {
			LOG.error(e.getMessage(), e);
			return e.asExitCode();
		}
	}
}
