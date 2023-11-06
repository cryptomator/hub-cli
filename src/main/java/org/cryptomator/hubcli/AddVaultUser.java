package org.cryptomator.hubcli;

import com.fasterxml.jackson.databind.ObjectMapper;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

@Command(name = "add-user",//
        description = "Add a user to a vault")
public class AddVaultUser implements Callable<Integer> {

    @Mixin
    Common common;

    @Mixin
    AccessToken accessToken;

    @Option(names = {"--vault-id"}, required = true, description = "id of the vault")
    String vaultId;

    @Option(names = {"--user-id"}, required = true, description = "id of an user")
    String userId;

    @Option(names = {"--owner"}, description = "if the users should be also a vault owner")
    boolean owner;

    @Override
    public Integer call() {
        try (var httpClient = HttpClient.newHttpClient()) {
            //add user
            var addUserUri = common.getApiBase().resolve("vaults/" + vaultId + "/users/" + userId + (owner ? "?role=owener" : ""));
            var addUserRequest = HttpRequest.newBuilder(addUserUri)
                    .header("Authorization", "Bearer " + accessToken) //
                    .PUT(HttpRequest.BodyPublishers.noBody()) //
                    //.timeout(REQ_TIMEOUT) //
                    .build();
            var response = httpClient.send(addUserRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.US_ASCII));
            if (response.statusCode() != 201) {
                System.err.println("Unexpected response when adding user: " + response.statusCode());
                return response.statusCode();
            }

            //get public key of user
            var getUserUri = common.getApiBase().resolve("authorities?ids=" + userId);
            var getUserRequest = HttpRequest.newBuilder(addUserUri)
                    .header("Authorization", "Bearer " + accessToken) //
                    .GET() //
                    //.timeout(REQ_TIMEOUT) //
                    .build();
            var getUserResponse = httpClient.send(getUserRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.US_ASCII));
            var getUserStatus = getUserResponse.statusCode();
            if (getUserStatus != 201) {
                System.err.println("Unexpected response when retrieving user info: " + getUserStatus);
                return getUserStatus;
            }
            var publicKey = new ObjectMapper().reader().readTree(getUserResponse.body()).get(0).get("public_key").asText();


            //get vault masterkey

            //grant access
            String jwe;
            var grantAccessUri = common.getApiBase().resolve("vaults/" + vaultId + "/access-tokens/" + userId);
            var request = HttpRequest.newBuilder(addUserUri)
                    .header("Authorization", "Bearer " + accessToken) //
                    .header("Content-Type", "text/plain")
                    .PUT(HttpRequest.BodyPublishers.ofString(jwe)) //
                    //.timeout(REQ_TIMEOUT) //
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
