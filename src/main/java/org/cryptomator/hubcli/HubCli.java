package org.cryptomator.hubcli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "hub-cli", //
        mixinStandardHelpOptions = true, //
        version = "hub-cli 4.0", //TODO: can we set this during build?
        description = "Manage Cryptomator Hub instances via CLI.", //
        subcommands = {ListVaults.class, CreateVault.class})
class HubCli {

    static final String URL_KEY = "HUB_CLI_URL";
    static final String CLIENT_SECRET_KEY = "HUB_CLI_CLIENT_SECRET";

    private static void validateEnv(CommandLine cli) {
        var env = System.getenv();
        if (!(env.containsKey(URL_KEY) && env.containsKey(CLIENT_SECRET_KEY))) {
            cli.getErr().println(cli.getColorScheme().errorText("Invalid Environment!"));
            System.exit(2);
        }
    }

    public static void main(String... args) {
        var app = new HubCli();
        var cli = new CommandLine(app);
        validateEnv(cli);
        System.exit(cli.execute(args));
    }

}