package org.cryptomator.hubcli;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.SignedJWT;
import picocli.CommandLine;

import java.text.ParseException;

class AccessToken {

	@CommandLine.Option(names = {"--access-token"}, required = true, interactive = true, description = "Access token, defaults to $HUB_CLI_ACCESS_TOKEN", defaultValue = "${env:HUB_CLI_ACCESS_TOKEN}", scope = CommandLine.ScopeType.INHERIT)
	public String value;

	public JWT parsed() throws ParseException {
		return SignedJWT.parse(value);
	}

}
