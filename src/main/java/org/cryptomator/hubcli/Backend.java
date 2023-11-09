package org.cryptomator.hubcli;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cryptomator.hubcli.UnexpectedStatusCodeException;
import picocli.CommandLine;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class Backend implements AutoCloseable {

    private final String accessToken;
    private final URI apiBase;

    private final VaultService vaultService;
    private final UserService userService;

    public Backend(AccessToken token, Common common) {
        this.accessToken = token.value;
        this.apiBase = common.getApiBase();

        this.vaultService = new VaultService();
        this.userService = new UserService();
    }

    public VaultService getVaultService() {
        return vaultService;
    }

    public UserService getUserService() {
        return userService;
    }

    class VaultService {

        public HttpResponse<String> createOrUpdateVault(UUID vaultId, String name, String description, boolean archived) throws IOException, InterruptedException, UnexpectedStatusCodeException {
            var vault = new VaultDto(vaultId,name, Objects.requireNonNullElse(description,""),archived,Instant.now().toString(),"asd",3,"asd","asd","asd"); //TODO: backend allows nullable description, but frontend is broken
            var req = createRequest("vaults/"+vaultId).PUT(HttpRequest.BodyPublishers.ofString(vault.toJson())).build();
            return sendRequest(HttpClient.newHttpClient(), req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8), 200, 201);
        }

        public HttpResponse<String> grantAccess(UUID vaultId, String userId, String jwe) throws IOException, InterruptedException, UnexpectedStatusCodeException {
            var req = createRequest("vaults/"+vaultId+"/access-tokens/"+userId).PUT(HttpRequest.BodyPublishers.ofString(jwe)).header("Content-Type", "text/plain").build();
            return sendRequest(HttpClient.newHttpClient(), req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8), 201);
        }

        public record VaultDto(@JsonProperty("id") UUID id,
                               @JsonProperty("name") String name,
                               @JsonProperty("description") String description,
                               @JsonProperty("archived") boolean archived,
                               @JsonProperty("creationTime") String creationTime, //TODO: To use instant we need additional jackson library
                               // Legacy properties ("Vault Admin Password"):
                               @JsonProperty("masterkey") String masterkey, @JsonProperty("iterations") Integer iterations,
                               @JsonProperty("salt") String salt,
                               @JsonProperty("authPublicKey") String authPublicKey, @JsonProperty("authPrivateKey") String authPrivateKey
        ) {

            public String toJson() throws JsonProcessingException {
                return new ObjectMapper().writeValueAsString(this);
            }

        }

    }

    class UserService {

        public UserDto getMe(boolean withDevices) throws IOException, InterruptedException, UnexpectedStatusCodeException {
            var req = createRequest("users/me?withDevices"+withDevices).GET().build();
            var body = sendRequest(HttpClient.newHttpClient(), req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8), 200).body();
            return UserDto.fromJsonString(body);
            //return new ObjectMapper().reader().readValue(body, Backend.UserService.UserDto.class);
        }

        public record UserDto(@JsonProperty("id") String id,
                              @JsonProperty("name") String name,
                              @JsonProperty("type") String type,
                              @JsonProperty("publicKey") String publicKey,
                              @JsonProperty("privateKey") String privateKey,
                              @JsonProperty("setupCode") String setupCode) {

            static UserDto fromJsonString(String json) throws JsonProcessingException {
                var userString = new ObjectMapper().reader().readTree(json);
                return new UserDto(userString.get("id").asText(), //
                        userString.get("name").asText(), //
                        userString.get("type").asText(), //
                        userString.get("publicKey").asText(), //
                        userString.get("privateKey").asText(), //
                        userString.get("setupCode").asText());
            }
        }

    }


    private HttpRequest.Builder createRequest(String path) {
        var uri = apiBase.resolve(path);
        return HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(5)).header("Authorization", "Bearer " + accessToken);
    }

    private <T> HttpResponse<T> sendRequest(HttpClient httpClient, HttpRequest request, HttpResponse.BodyHandler<T> bodyHandler, int... expectedStatusCode) throws IOException, InterruptedException, UnexpectedStatusCodeException {
        var res = httpClient.send(request, bodyHandler);
        var status = res.statusCode();
        if (Arrays.stream(expectedStatusCode).noneMatch(s -> s == status)) {
            throw new UnexpectedStatusCodeException(status, "Unexpected response for " + request.method() + " " + request.uri() + ": " + status);
        }
        return res;
    }

    @Override
    public void close() {

    }
}
