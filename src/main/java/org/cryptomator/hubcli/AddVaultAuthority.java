package org.cryptomator.hubcli;

import picocli.CommandLine.Option;
import picocli.CommandLine.Command;

@Command(name = "add-vaultauthority",//
        description = "Add a user or group to a vault" )
class AddVaultAuthority implements Runnable {

    @Option(names = { "--vault-id" }, required = true, description = "id of the vault")
    String vaultId;

    @Option(names = { "--authority-id" }, required = true, description = "id of an authority")
    String authority;

    @Option(names = { "--owner" }, description = "description of the vault")
    boolean owner;

    @Override
    public void run() {
        //add authority to vault

    }
}
