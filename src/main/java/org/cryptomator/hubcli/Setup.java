package org.cryptomator.hubcli;

import picocli.CommandLine.Option;
import picocli.CommandLine.Command;

@Command(name = "setup",//
        description = "setup this device to use hub cli app" )
public class Setup implements Runnable{

    @Option(names = { "--output-devicekey" }, required = true, description = "where to store the generated device key")
    String path;

    @Override
    public void run() {
        // setup code (as in frontend)
        System.out.println("setup code");
        // create device keypair as in Cryptomator
        // PUT auf /user/me
        // PUT /devices/this-device-id (hash from public key)

    }
}
