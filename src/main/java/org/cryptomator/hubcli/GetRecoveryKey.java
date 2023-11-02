package org.cryptomator.hubcli;

import picocli.CommandLine.Option;
import picocli.CommandLine.Command;

@Command(name = "get-recoverykey",//
        description = "Prints the recovery key of a vault to stdout" )
class GetRecoveryKey implements Runnable{
    @Option(names = { "--vaultId" }, required = true, description = "id of the vault")
    String vaultId;

    @Override
    public void run() {

    }
}
