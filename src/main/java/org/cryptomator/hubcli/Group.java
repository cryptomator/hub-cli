package org.cryptomator.hubcli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(name = "group",
		description = "Manage user.")
class Group {

	private static final Logger LOG = LoggerFactory.getLogger(Group.class);

	@CommandLine.Mixin
	Common common;

	@CommandLine.Mixin
	AccessToken accessToken;

	@CommandLine.Command(name = "list", description = "List all groups.")
	public Integer call() throws InterruptedException, IOException {
		try (var backend = new Backend(accessToken.value, common.getApiBase())) {
			var accessible = backend.getGroupService().listAll().body();
			System.out.println(accessible);
			return 0;
		} catch (UnexpectedStatusCodeException e) {
			LOG.error(e.getMessage(), e);
			return e.asExitCode();
		}
	}
}
