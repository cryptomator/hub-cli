package org.cryptomator.hubcli;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

class Common {

	private static final Logger LOG = LoggerFactory.getLogger(Common.class);

	@Option(names = {"--api-base"}, required = true, description = "API base URL of Cryptomator Hub, defaults to $HUB_CLI_API_BASE", defaultValue = "${env:HUB_CLI_API_BASE}", scope = CommandLine.ScopeType.INHERIT)
	private URI apiBase;

	private Config config;

	public Config getConfig() throws IOException, InterruptedException {
		if (config == null) {
			try (var client = HttpClient.newHttpClient()) {
				var uri = getApiBase().resolve("config");
				var req = HttpRequest.newBuilder().GET().uri(uri).build();
				var res = client.send(req, HttpResponse.BodyHandlers.ofString());
				if (res.statusCode() != 200) {
					LOG.error("Unexpected response for {} {}: {}", res.request().method(), res.request().uri(), res.statusCode());
					System.exit(res.statusCode());
				}
				var json = new ObjectMapper().reader().readTree(res.body());
				config = new Config(json);
			}
		}
		return config;
	}

	public URI getApiBase() {
		// make sure to always end on "/":
		return URI.create(apiBase.toString() + "/").normalize();
	}

	record Config(JsonNode config) {

		private static final String AUTH_ENDPOINT_KEY = "keycloakAuthEndpoint";
		private static final String TOKEN_ENDPOINT_KEY = "keycloakTokenEndpoint";
		private static final String API_LEVEL_KEY = "apiLevel";

		public URI getAuthEndpoint() {
			return URI.create(config.get(AUTH_ENDPOINT_KEY).asText());
		}

		public URI getTokenEndpoint() {
			return URI.create(config.get(TOKEN_ENDPOINT_KEY).asText());
		}

		public int getAPILevel() {
			return config.get(API_LEVEL_KEY).asInt();
		}

	}


}
