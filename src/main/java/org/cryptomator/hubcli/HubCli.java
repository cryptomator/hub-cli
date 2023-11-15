package org.cryptomator.hubcli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "hub", //
		mixinStandardHelpOptions = true, //
		version = "Hub CLI 1.3.0.0", //TODO: can we set this during build?
		description = "Manage Cryptomator Hub instances via CLI.", //
		subcommands = {Login.class, Setup.class, Vault.class, Group.class, User.class})
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