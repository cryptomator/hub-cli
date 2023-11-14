package org.cryptomator.hubcli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.IOException;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "list-vaults", description = "List owned vaults.")
class ListVaults implements Callable<Integer> {

	private static final Logger LOG = LoggerFactory.getLogger(ListVaults.class);

	@CommandLine.Mixin
	Common common;

	@CommandLine.Mixin
	AccessToken accessToken;

	@Override
	public Integer call() throws InterruptedException, IOException {
		try (var backend = new Backend(accessToken.value, common.getApiBase())) {
			var accessible = backend.getVaultService().listAccessible();
			System.out.println(accessible);
			return 0;
		} catch (UnexpectedStatusCodeException e) {
			LOG.error(e.getMessage(), e);
			return e.status;
		}
	}
}
