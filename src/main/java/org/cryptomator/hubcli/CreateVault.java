package org.cryptomator.hubcli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "create-vault",//
        description = "Create a new vault" )
class CreateVault implements Runnable {
    @Override
    public void run() {
        System.out.println("This is a stub too!");
    }
}
