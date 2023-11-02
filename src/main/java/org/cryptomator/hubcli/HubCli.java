package org.cryptomator.hubcli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "hub-cli", //
        mixinStandardHelpOptions = true, //
        version = "hub-cli 4.0", //TODO: can we set this during build?
        description = "Manage Cryptomator Hub instances via CLI.", //
        subcommands = {Login.class, ListVaults.class, CreateVault.class, UpdateVault.class})
class HubCli {

    static final String CLIENT_ID_KEY = "HUB_CLIENT_ID";
    static final String CLIENT_SECRET_KEY = "HUB_CLI_CLIENT_SECRET";
    static final String TOKEN_ENDPOINT_KEY = "HUB_CLI_TOKEN_ENDPOINT";
    static final String API_BASE_KEY = "HUB_CLI_API_BASE";
    static final String ACCESS_TOKEN_KEY = "ACCESS_TOKEN";

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