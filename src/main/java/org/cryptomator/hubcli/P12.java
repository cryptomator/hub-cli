package org.cryptomator.hubcli;

import picocli.CommandLine;

import java.nio.file.Path;

class P12 {

	@CommandLine.Option(names = {"-f", "--p12-file"}, required = true, description = "Where to store the generated device key pair. Defaults to $HUB_CLI_P12_FILE", defaultValue = "${env:HUB_CLI_P12_FILE}")
	Path file;

	@CommandLine.Option(names = {"-p", "--p12-password"}, required = true, interactive = true, description = "Password protecting the device key pair. Defaults to $HUB_CLI_P12_PASSWORD", defaultValue = "${env:HUB_CLI_P12_PASSWORD}")
	char[] password;

}
