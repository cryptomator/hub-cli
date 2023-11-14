package org.cryptomator.hubcli;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.coffeelibs.tinyoauth2client.TinyOAuth2;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.concurrent.Callable;

import static java.nio.charset.StandardCharsets.UTF_8;

@Command(name = "login", description = "Login to the hub instance and retrieve an access token.", subcommands = {Login.ClientCredentials.class, Login.AuthorizationCode.class})
class Login {

	@Option(names = {"--client-id"}, required = true, description = "Client Id, defaults to $HUB_CLI_CLIENT_ID", defaultValue = "${env:HUB_CLI_CLIENT_ID}")
	String clientId;

	@Command(name = "client-credentials", description = "Use the Client Credentials Flow.")
	static class ClientCredentials implements Callable<Integer> {

		@Mixin
		Common common;

		@CommandLine.ParentCommand
		Login login;

		@Option(names = {"--client-secret"}, required = true, interactive = true, description = "Client Secret, defaults to $HUB_CLI_CLIENT_SECRET", defaultValue = "${env:HUB_CLI_CLIENT_SECRET}")
		char[] clientSecret;

		@Override
		public Integer call() throws Exception {
			var authResponse = TinyOAuth2.client(login.clientId) //
					.withTokenEndpoint(common.getConfig().getTokenEndpoint()) //
					.clientCredentialsGrant(UTF_8, CharBuffer.wrap(clientSecret)) //
					.authorize(HttpClient.newHttpClient());

			Arrays.fill(clientSecret, ' ');

			return printAccessToken(authResponse);
		}

	}

	@Command(name = "authorization-code", description = "Use the Authorization Code Flow with PKCE.")
	static class AuthorizationCode implements Callable<Integer> {

		@CommandLine.ParentCommand
		Login login;

		@Mixin
		Common common;

		@Override
		public Integer call() throws Exception {
			var authResponse = TinyOAuth2.client(login.clientId) //
					.withTokenEndpoint(common.getConfig().getTokenEndpoint()) //
					.authorizationCodeGrant(common.getConfig().getAuthEndpoint()) //
					.authorize(HttpClient.newHttpClient(), uri -> {
						System.out.println("Please login on " + uri);
					});

			return printAccessToken(authResponse);
		}

	}


	private static int printAccessToken(HttpResponse<String> response) throws JsonProcessingException {
		var statusCode = response.statusCode();
		if (statusCode != 200) {
			System.err.println("Unexpected response for " + response.request().method() + " " + response.request().uri() + ": " + response.statusCode());
			return statusCode;
		}

		var token = new ObjectMapper().reader().readTree(response.body()).get("access_token").asText();
		System.out.println(token);
		return 0;
	}

}

