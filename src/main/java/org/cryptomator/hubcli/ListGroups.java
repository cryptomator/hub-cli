package org.cryptomator.hubcli;

import picocli.CommandLine;

import java.io.IOException;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "list-groups", description = "List all groups.")
class ListGroups implements Callable<Integer> {

	@CommandLine.Mixin
	Common common;

	@CommandLine.Mixin
	AccessToken accessToken;

	@Override
	public Integer call() throws InterruptedException, IOException {
		try (var backend = new Backend(accessToken.value, common.getApiBase())) {
			var accessible = backend.getGroupService().listAll();
			System.out.println(accessible);
			return 0;
		} catch (UnexpectedStatusCodeException e) {
			System.err.println(e.getMessage());
			return e.status;
		}
	}
}
