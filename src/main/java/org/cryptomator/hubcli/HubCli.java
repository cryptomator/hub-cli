package org.cryptomator.hubcli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "hub-cli", //
		mixinStandardHelpOptions = true, //
		version = "hub-cli 1.3.0.0", //TODO: can we set this during build?
		description = "Manage Cryptomator Hub instances via CLI.", //
		subcommands = {Login.class, CreateVault.class, UpdateVault.class, //
				GetRecoveryKey.class, AddVaultUser.class, AddVaultGroup.class, //
				ListVaults.class, ListGroups.class, ListUsers.class, //
				RemoveVaultAuthority.class, Setup.class //
		})
class HubCli {

	private static void validate(CommandLine cli) {
		var env = System.getenv();
		if (env.containsKey("test")) {
			cli.getErr().println(cli.getColorScheme().errorText("\"Test\" found in environment!"));
			System.exit(2);
		}
	}

	public static void main(String... args) {
		var app = new HubCli();
		var cli = new CommandLine(app);
		validate(cli);
		System.exit(cli.execute(args));
	}

}