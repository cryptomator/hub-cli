package org.cryptomator.hubcli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Callable;

@Command(name = "remove-vaultauthority",//
		description = "Remove a user or group from a vault")
class RemoveVaultAuthority implements Callable<Integer> {

	private static final Logger LOG = LoggerFactory.getLogger(RemoveVaultAuthority.class);

	@Mixin
	Common common;

	@Mixin
	AccessToken accessToken;

	@Option(names = {"--vault-id"}, required = true, description = "id of the vault")
	UUID vaultId;

	@Option(names = {"--authority-id"}, required = true, description = "id of an authority")
	String authorityId;

	@Override
	public Integer call() throws InterruptedException, IOException {
		try (var backend = new Backend(accessToken.value, common.getApiBase())) {
			backend.getVaultService().removeAuthority(vaultId, authorityId);
			return 0;
		} catch (UnexpectedStatusCodeException e) {
			LOG.error(e.getMessage(), e);
			return e.status;
		}
	}
}
