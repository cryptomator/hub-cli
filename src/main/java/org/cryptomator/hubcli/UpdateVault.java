package org.cryptomator.hubcli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "update-vault", //
        description = "Update certain vault properties" )
class UpdateVault implements Runnable {

    @Option(names = { "--vault-id" }, required = true, description = "id of the vault")
    String vaultId;
    @Option(names = { "--name" }, description = "name of the vault")
    String name;
    @Option(names = { "--description" }, description = "description of the vault")
    String description;
    @Option(names = { "--archive" }, negatable = true, description = "(de-)archives the vault")
    boolean archive;

    @Override
    public void run() {
        //1. get vault() to get current status
        //2. set vault values to change()

    }
}
