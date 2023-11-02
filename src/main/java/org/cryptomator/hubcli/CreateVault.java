package org.cryptomator.hubcli;

import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Command;

@Command(name = "create-vault",//
        description = "Create a new vault" )
class CreateVault implements Runnable {

    @Option(names = { "--vault-id" }, required = true, description = "id of the vault")
    String vaultId;
    @Option(names = { "--name" }, required = true, description = "name of the vault")
    String name;
    @Option(names = { "--description" }, description = "description of the vault")
    String description;
    @Option(names = { "--output-file" }, required = true, description = "path where to store the returned vault template")
    String outpoutFile;
    @Option(names = { "--no-recovery-key" }, description = "suppress output of recovery key")
    boolean noRecoveryKey;

    @Override
    public void run() {
        //generate masterkey, itartions, salt

        if(!noRecoveryKey) {
            //show recovery key on std.out
            System.out.println("Recovery key");
        }
        //download template and save it to outputFile
        return;
    }
}
