package org.cryptomator.hubcli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;

@Command(name = "update-vault", //
		description = "Update certain vault properties")
class UpdateVault implements Callable<Integer> {

	@Mixin
	Common common;

	@Mixin
	AccessToken accessToken;

	@Option(names = {"--vault-id"}, required = true, description = "id of the vault")
	String vaultId;
	@Option(names = {"--name"}, description = "name of the vault")
	String name;
	@Option(names = {"--description"}, description = "description of the vault")
	String description;
	@Option(names = {"--archive"}, negatable = true, description = "(de-)archives the vault")
	Optional<Boolean> archive;

	@Override
	public Integer call() throws Exception {
		try (var backend = new Backend(accessToken.value, common.getApiBase())) {
			var vaultUuid = UUID.fromString(vaultId);
			var vault = backend.getVaultService().get(vaultUuid);
			backend.getVaultService().createOrUpdateVault(vaultUuid, //
					Objects.requireNonNullElse(name, vault.name()), //
					Objects.requireNonNullElse(description, vault.description()), //
					archive.orElse(vault.archived()));
		} catch (UnexpectedStatusCodeException e) {
			System.err.println(e.getMessage());
			return e.status;
		}
		return 0;
	}
}
