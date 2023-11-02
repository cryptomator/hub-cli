package org.cryptomator.hubcli;

import picocli.CommandLine;

@CommandLine.Command(name = "list-vaults",
        description = "List all vaults.")
class ListVaults implements Runnable {

    //TODO: ideas
    // --format: [json | csv]
    // --properties: list of properties to output

    @CommandLine.Option(names = { "--full" }, description = "Print all properties of each vault")
    boolean fullInfo;

    @Override
    public void run() {
        //http request
        //parse response
        if(fullInfo) {
            //everything
        } else {
           //only output name, timestamp, uuid
        }
        System.out.println("This is a stub");
    }
}
