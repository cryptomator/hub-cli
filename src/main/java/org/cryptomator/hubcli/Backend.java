package org.cryptomator.hubcli;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cryptomator.hubcli.model.UserDto;
import org.cryptomator.hubcli.model.VaultDto;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;

public class Backend implements AutoCloseable {

    private final String accessToken;
    private final URI apiBase;

    private final VaultService vaultService;
    private final UserService userService;

    public Backend(String accessToken, URI apiBase) {
        this.accessToken = accessToken;
        this.apiBase = apiBase;

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
            //creationTime is ignored
            var vault = new VaultDto(vaultId, name, description, archived, "1970-01-01T00:00:00Z", null, 0, null, null, null);
            var req = createRequest("vaults/" + vaultId).PUT(HttpRequest.BodyPublishers.ofString(vault.toJson())).build();
            return sendRequest(HttpClient.newHttpClient(), req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8), 200, 201);
        }

        public HttpResponse<String> grantAccess(UUID vaultId, String userId, String jwe) throws IOException, InterruptedException, UnexpectedStatusCodeException {
            var req = createRequest("vaults/" + vaultId + "/access-tokens/" + userId).PUT(HttpRequest.BodyPublishers.ofString(jwe)).header("Content-Type", "text/plain").build();
            return sendRequest(HttpClient.newHttpClient(), req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8), 201);
        }

    }

    class UserService {

        public UserDto getMe(boolean withDevices) throws IOException, InterruptedException, UnexpectedStatusCodeException {
            var req = createRequest("users/me?withDevices=" + withDevices).GET().build();
            var body = sendRequest(HttpClient.newHttpClient(), req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8), 200).body();
            return new ObjectMapper().reader().readValue(body, UserDto.class);
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
        //close http client?
    }
}
