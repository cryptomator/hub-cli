package org.cryptomator.hubcli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "add-group",//
		description = "Add a group to a vault")
class AddVaultGroup implements Runnable {

	@Option(names = {"--vault-id"}, required = true, description = "id of the vault")
	String vaultId;

	@Option(names = {"--group-id"}, required = true, description = "id of a group")
	String authority;

	@Override
	public void run() {
		//add group to vault
		//grant access by requesting all group users and grant for each access
	}
}
