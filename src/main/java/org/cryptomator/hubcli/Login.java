package org.cryptomator.hubcli;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.coffeelibs.tinyoauth2client.TinyOAuth2;
import org.cryptomator.hubcli.util.HttpClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.net.http.HttpResponse;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.concurrent.Callable;

import static java.nio.charset.StandardCharsets.UTF_8;

@Command(name = "login", description = "Login to the hub instance and retrieve an access token.", subcommands = {Login.ClientCredentials.class, Login.AuthorizationCode.class})
class Login {

	private static final Logger LOG = LoggerFactory.getLogger(Login.class);

	@Option(names = {"--client-id"}, required = true, description = "Client Id, defaults to $HUB_CLI_CLIENT_ID", defaultValue = "${env:HUB_CLI_CLIENT_ID}", scope = CommandLine.ScopeType.INHERIT)
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
			try (var client = HttpClientFactory.create()) {
				var authResponse = TinyOAuth2.client(login.clientId) //
						.withTokenEndpoint(common.getConfig().getTokenEndpoint()) //
						.clientCredentialsGrant(UTF_8, CharBuffer.wrap(clientSecret)) //
						.authorize(client);
				printAccessToken(authResponse);
				return 0;
			} catch (UnexpectedStatusCodeException e) {
				LOG.error(e.getMessage());
				return e.asExitCode();
			} finally {
				Arrays.fill(clientSecret, ' ');
			}
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
			try (var client = HttpClientFactory.create()) {
				var authResponse = TinyOAuth2.client(login.clientId) //
						.withTokenEndpoint(common.getConfig().getTokenEndpoint()) //
						.authorizationCodeGrant(common.getConfig().getAuthEndpoint()) //
						.authorize(client, uri -> {
							System.out.println("Please login on " + uri);
						});
				printAccessToken(authResponse);
				return 0;
			} catch (UnexpectedStatusCodeException e) {
				LOG.error(e.getMessage());
				return e.asExitCode();
			}
		}

	}


	private static void printAccessToken(HttpResponse<String> response) throws JsonProcessingException, UnexpectedStatusCodeException {
		var statusCode = response.statusCode();
		if (statusCode != 200) {
			throw new UnexpectedStatusCodeException(statusCode, "Unexpected response for " + response.request().method() + " " + response.request().uri() + ": " + statusCode);
		}

		var token = new ObjectMapper().reader().readTree(response.body()).get("access_token").asText();
		System.out.println(token);
	}

}

