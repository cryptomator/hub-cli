package org.cryptomator.hubcli;

import picocli.CommandLine;

import java.io.IOException;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "list-vaults", description = "List owned vaults.")
class ListVaults implements Callable<Integer> {

	@CommandLine.Mixin
	Common common;

	@CommandLine.Mixin
	AccessToken accessToken;

	@Override
	public Integer call() throws InterruptedException, IOException {
		try (var backend = new Backend(accessToken.value, common.getApiBase())) {
			var accessible = backend.getVaultService().listAccessible().body();
			System.out.println(accessible);
			return 0;
		} catch (UnexpectedStatusCodeException e) {
			return e.status;
		}
	}
}
