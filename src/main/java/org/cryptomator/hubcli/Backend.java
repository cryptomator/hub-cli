package org.cryptomator.hubcli;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.cryptomator.hubcli.model.DeviceDto;
import org.cryptomator.hubcli.model.UserDto;
import org.cryptomator.hubcli.model.VaultDto;
import org.cryptomator.hubcli.model.VaultRole;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Backend implements AutoCloseable {

    private final String accessToken;
    private final URI apiBase;

    private final VaultService vaultService;
    private final UserService userService;
    private final DeviceService deviceService;

    private final AuthorityService authorityService;

    private final HttpClient httpClient;

    private final ObjectMapper objectMapper;

    public Backend(String accessToken, URI apiBase) {
        this.accessToken = accessToken;
        this.apiBase = apiBase;

        this.httpClient = HttpClient.newHttpClient();

        this.vaultService = new VaultService();
        this.userService = new UserService();
        this.deviceService = new DeviceService();
        this.authorityService = new AuthorityService();

        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    public VaultService getVaultService() {
        return vaultService;
    }

    public UserService getUserService() {
        return userService;
    }

    public DeviceService getDeviceService() {
        return deviceService;
    }

    public AuthorityService getAuthorityService() {
        return authorityService;
    }

    class VaultService {

        public VaultDto get(UUID vaultId) throws IOException, InterruptedException, UnexpectedStatusCodeException {
            var req = createRequest("vaults/" + vaultId).GET().build();
            var res = sendRequest(httpClient, req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8), 200);
            return objectMapper.readValue(res.body(), VaultDto.class);
        }

        public List<VaultDto> getSome(UUID... vaultId) throws IOException, InterruptedException, UnexpectedStatusCodeException {
            var queryParams = Arrays.stream(vaultId).map(UUID::toString).collect(Collectors.joining("&ids="));
            var req = createRequest("vaults/some?ids=" + queryParams).GET().build();
            var res = sendRequest(httpClient, req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8), 200);
            return objectMapper.readValue(res.body(), new TypeReference<List<VaultDto>>() {});
        }

        public HttpResponse<String> createOrUpdateVault(UUID vaultId, String name, String description, boolean archived) throws IOException, InterruptedException, UnexpectedStatusCodeException {
            //creationTime is ignored
            var vault = new VaultDto(vaultId, name, description, archived, "1970-01-01T00:00:00Z", null, 0, null, null, null);
            var req = createRequest("vaults/" + vaultId).PUT(HttpRequest.BodyPublishers.ofString(vault.toJson())).build();
            return sendRequest(httpClient, req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8), 200, 201);
        }

        public HttpResponse<Void> addUser(UUID vaultId, String userId, VaultRole vaultRole) throws IOException, InterruptedException, UnexpectedStatusCodeException {
            var addUserReq = createRequest("vaults/" + vaultId + "/users/" + userId + "?role=" + vaultRole.name()).PUT(HttpRequest.BodyPublishers.noBody()).build();
            return sendRequest(httpClient, addUserReq, HttpResponse.BodyHandlers.discarding(), 200, 201);
        }

        public HttpResponse<String> grantAccess(UUID vaultId, String userId, String jwe) throws IOException, InterruptedException, UnexpectedStatusCodeException {
            var req = createRequest("vaults/" + vaultId + "/access-tokens/" + userId).PUT(HttpRequest.BodyPublishers.ofString(jwe)).header("Content-Type", "text/plain").build();
            return sendRequest(httpClient, req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8), 201);
        }

        public HttpResponse<String> getAccessToken(UUID vaultId) throws IOException, InterruptedException, UnexpectedStatusCodeException {
            var vaultKeyReq = createRequest("vaults/" + vaultId + "/access-token").GET().build();
            return sendRequest(httpClient, vaultKeyReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.US_ASCII), 200);
        }

    }

    class UserService {

        public UserDto getMe(boolean withDevices) throws IOException, InterruptedException, UnexpectedStatusCodeException {
            var req = createRequest("users/me?withDevices=" + withDevices).GET().build();
            var body = sendRequest(httpClient, req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8), 200).body();
            return objectMapper.readValue(body, UserDto.class);
        }

    }

    class DeviceService {

        public DeviceDto get(String deviceId) throws IOException, InterruptedException, UnexpectedStatusCodeException {
            var deviceReq = createRequest("devices/" + deviceId).GET().build();
            var deviceRes = sendRequest(httpClient, deviceReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8), 200);
            return objectMapper.reader().readValue(deviceRes.body(), DeviceDto.class);
        }
    }

    class AuthorityService {

        public List<UserDto> listSome(String userId) throws IOException, InterruptedException, UnexpectedStatusCodeException {
            var memberInfoReq = createRequest("authorities?ids=" + userId).GET().build();
            var memberInfoRes = sendRequest(httpClient, memberInfoReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8), 200);
            return objectMapper.readerForListOf(UserDto.class).<List<UserDto>>readValue(memberInfoRes.body());
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
        httpClient.close();
    }
}
