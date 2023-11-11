package org.cryptomator.hubcli;

import picocli.CommandLine;

import java.io.IOException;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "list-users", description = "List all users.")
class ListUsers implements Callable<Integer> {

	@CommandLine.Mixin
	Common common;

	@CommandLine.Mixin
	AccessToken accessToken;

	@Override
	public Integer call() throws InterruptedException, IOException {
		try (var backend = new Backend(accessToken.value, common.getApiBase())) {
			var accessible = backend.getUserService().listAll();
			System.out.println(accessible);
			return 0;
		} catch (UnexpectedStatusCodeException e) {
			return e.status;
		}
	}
}