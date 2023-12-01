package org.cryptomator.hubcli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;

@Command(name = "update", description = "Update certain vault properties")
class VaultUpdate implements Callable<Integer> {

	private static final Logger LOG = LoggerFactory.getLogger(VaultUpdate.class);

	@CommandLine.ParentCommand
	Vault parentCmd;

	@Option(names = {"--vault-id", "-v"}, required = true, description = "id of the vault")
	UUID vaultId;

	@Option(names = {"--name"}, description = "name of the vault")
	String name;

	@Option(names = {"--description"}, description = "description of the vault")
	String description;

	@Option(names = {"--archive"}, negatable = true, description = "(de-)archives the vault")
	Optional<Boolean> archive;

	@Override
	public Integer call() throws Exception {
		try (var backend = new Backend(parentCmd.accessToken.value, parentCmd.common.getApiBase())) {
			var vault = backend.getVaultService().get(vaultId);
			backend.getVaultService().createOrUpdateVault(vaultId, //
					Objects.requireNonNullElse(name, vault.name()), //
					Objects.requireNonNullElse(description, vault.description()), //
					archive.orElse(vault.archived()));
		} catch (UnexpectedStatusCodeException e) {
			LOG.error(e.getMessage(), e);
			return e.asExitCode();
		}
		return 0;
	}
}
