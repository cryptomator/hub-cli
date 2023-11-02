package org.cryptomator.hubcli;

import picocli.CommandLine.Option;
import picocli.CommandLine.Command;

@Command(name = "add-user",//
        description = "Add a user to a vault" )
public class AddVaultUser implements Runnable {

    @Option(names = { "--vault-id" }, required = true, description = "id of the vault")
    String vaultId;

    @Option(names = { "--user-id" }, required = true, description = "id of an user")
    String authority;

    @Option(names = { "--owner" }, description = "if the users should be also a vault owner")
    boolean owner;

    @Override
    public void run() {
        //add user to vault
        //grant access
    }
}
