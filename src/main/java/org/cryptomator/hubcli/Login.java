package org.cryptomator.hubcli;

import io.github.coffeelibs.tinyoauth2client.TinyOAuth2;
import picocli.CommandLine;

import java.net.URI;
import java.net.http.HttpClient;
import java.util.Objects;
import java.util.concurrent.Callable;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.cryptomator.hubcli.HubCli.*;

@CommandLine.Command(name = "login",
        description = "Login to the hub instance and retrieve an access token.")
class Login implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        var env = System.getenv();
        String clientId = env.get(CLIENT_ID_KEY);
        String clientSecret = env.get(CLIENT_SECRET_KEY);
        String tokenEndpointUrl = env.get(TOKEN_ENDPOINT_KEY);
        if (clientId == null || clientSecret == null || tokenEndpointUrl == null) {
            System.err.println("Required environment variables %s, %s or %s could not be found in environment.".formatted(CLIENT_ID_KEY, CLIENT_SECRET_KEY, TOKEN_ENDPOINT_KEY));
            return 3;
        }

        var authResponse = TinyOAuth2.client(clientId)
                .withTokenEndpoint(URI.create(tokenEndpointUrl))
                .clientCredentialsGrant(UTF_8, clientSecret)
                .authorize(HttpClient.newHttpClient());

        var statusCode = authResponse.statusCode();
        if (statusCode != 200) {
            System.err.println("""
            Request was responded with code %d and body:
            %s
            """.formatted(statusCode, authResponse.body()));
            return statusCode;
        }

        System.out.println(extractAccessToken(authResponse.body()));
        return 0;
    }

    private String extractAccessToken(String jsonString) {
        var key = "access_token";

        var index= jsonString.indexOf(key)+key.length();
        return "asd";
    }
}

