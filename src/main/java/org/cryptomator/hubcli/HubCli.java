package org.cryptomator.hubcli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "hub-cli", //
        mixinStandardHelpOptions = true, //
        version = "hub-cli 4.0", //TODO: can we set this during build?
        description = "Manage Cryptomator Hub instances via CLI.", //
        subcommands = {ListVaults.class, CreateVault.class})
class HubCli {

    // this example implements Callable, so parsing, error handling and handling user
    // requests for usage help or version help can be done with one line of code.
    public static void main(String... args) {
        int exitCode = new CommandLine(new HubCli()).execute(args);
        System.exit(exitCode);
    }
}