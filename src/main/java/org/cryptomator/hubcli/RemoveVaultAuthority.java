package org.cryptomator.hubcli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "remove-vaultauthority",//
        description = "Remove a user or group from a vault" )
class RemoveVaultAuthority implements Runnable {

    @Option(names = { "--vault-id" }, required = true, description = "id of the vault")
    String vaultId;

    @Option(names = { "--authority-id" }, required = true, description = "id of an authority")
    String authorityId;

    @Override
    public void run() {
        //remove authority to vault
    }
}
