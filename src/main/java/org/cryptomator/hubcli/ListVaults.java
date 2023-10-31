package org.cryptomator.hubcli;

import picocli.CommandLine;

@CommandLine.Command(name = "list-vaults",
        description = "List all vaults.")
class ListVaults implements Runnable {


    @Override
    public void run() {
        System.out.println("This is a stub");
    }
}
