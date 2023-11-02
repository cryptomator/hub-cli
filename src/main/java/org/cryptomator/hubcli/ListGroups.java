package org.cryptomator.hubcli;

import picocli.CommandLine;

@CommandLine.Command(name = "list-groups",
        description = "List all groups.")
class ListGroups implements Runnable {

    //TODO: ideas
    // --format: [json | csv]
    // --properties: list of properties to output

    @CommandLine.Option(names = { "--full" }, description = "Print all properties of each group")
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
