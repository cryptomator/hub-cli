package org.cryptomator.hubcli.util;

import picocli.CommandLine;

public class ManifestVersionProvider implements CommandLine.IVersionProvider {

	@Override
	public String[] getVersion() {
		return new String[]{ "${COMMAND-FULL-NAME} version " + getImplementationVersion() };
	}

	public static String getImplementationVersion() {
		return ManifestVersionProvider.class.getPackage().getImplementationVersion();
	}
}
