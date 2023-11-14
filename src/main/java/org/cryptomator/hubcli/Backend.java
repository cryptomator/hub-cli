package org.cryptomator.hubcli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
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
import java.util.Map;
import java.util.UUID;

class Backend implements AutoCloseable {

	private final String accessToken;
	private final URI apiBase;

	private final VaultService vaultService;
	private final UserService userService;
	private final DeviceService deviceService;

	private final AuthorityService authorityService;

	private final GroupService groupService;

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
		this.groupService = new GroupService();

		this.objectMapper = JsonMapper.builder().findAndAddModules().build();
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

	public GroupService getGroupService() {
		return groupService;
	}

	class VaultService {

		public VaultDto get(UUID vaultId) throws IOException, InterruptedException, UnexpectedStatusCodeException {
			var req = createRequest("vaults/" + vaultId).GET().build();
			var res = sendRequest(httpClient, req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8), 200);
			return objectMapper.readValue(res.body(), VaultDto.class);
		}

		public HttpResponse<String> listAccessible() throws IOException, InterruptedException, UnexpectedStatusCodeException {
			var req = createRequest("vaults/accessible?role=OWNER").GET().build();
			return sendRequest(httpClient, req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8), 200);
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

		public HttpResponse<Void> addGroup(UUID vaultId, String groupId, VaultRole vaultRole) throws IOException, InterruptedException, UnexpectedStatusCodeException {
			var addUserReq = createRequest("vaults/" + vaultId + "/groups/" + groupId + "?role=" + vaultRole.name()).PUT(HttpRequest.BodyPublishers.noBody()).build();
			return sendRequest(httpClient, addUserReq, HttpResponse.BodyHandlers.discarding(), 200, 201);
		}

		public HttpResponse<String> grantAccess(UUID vaultId, Map<String, String> memberAccessTokens) throws IOException, InterruptedException, UnexpectedStatusCodeException {
			var body = objectMapper.writer().writeValueAsString(memberAccessTokens);
			var req = createRequest("vaults/" + vaultId + "/access-tokens").POST(HttpRequest.BodyPublishers.ofString(body)).build();
			return sendRequest(httpClient, req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8), 200, 409);
		}

		public HttpResponse<String> getAccessToken(UUID vaultId) throws IOException, InterruptedException, UnexpectedStatusCodeException {
			var vaultKeyReq = createRequest("vaults/" + vaultId + "/access-token").GET().build();
			return sendRequest(httpClient, vaultKeyReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.US_ASCII), 200);
		}

		public HttpResponse<Void> removeAuthority(UUID vaultId, String authorityId) throws IOException, InterruptedException, UnexpectedStatusCodeException {
			var req = createRequest("vaults/" + vaultId + "/authority/" + authorityId).DELETE().build();
			return sendRequest(httpClient, req, HttpResponse.BodyHandlers.discarding(), 204);
		}

	}

	class UserService {

		public HttpResponse<Void> createOrUpdateMe(UserDto user) throws IOException, InterruptedException, UnexpectedStatusCodeException {
			var body = objectMapper.writeValueAsString(user);
			var req = createRequest("users/me").PUT(HttpRequest.BodyPublishers.ofString(body)).build();
			return sendRequest(httpClient, req, HttpResponse.BodyHandlers.discarding(), 201);
		}

		public UserDto getMe(boolean withDevices) throws IOException, InterruptedException, UnexpectedStatusCodeException {
			var req = createRequest("users/me?withDevices=" + withDevices).GET().build();
			var body = sendRequest(httpClient, req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8), 200).body();
			return objectMapper.readValue(body, UserDto.class);
		}

		public HttpResponse<String> listAll() throws IOException, InterruptedException, UnexpectedStatusCodeException {
			var req = createRequest("users").GET().build();
			return sendRequest(httpClient, req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8), 200);
		}

	}

	class GroupService {

		public HttpResponse<String> listAll() throws IOException, InterruptedException, UnexpectedStatusCodeException {
			var req = createRequest("groups").GET().build();
			return sendRequest(httpClient, req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8), 200);
		}

		public List<UserDto> getEffectiveMembers(String groupId) throws IOException, InterruptedException, UnexpectedStatusCodeException {
			var req = createRequest("groups/" + groupId + "/effective-members").GET().build();
			var body = sendRequest(httpClient, req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8), 200).body();
			return objectMapper.readerForListOf(UserDto.class).readValue(body);
		}
	}

	class DeviceService {

		public DeviceDto get(String deviceId) throws IOException, InterruptedException, UnexpectedStatusCodeException {
			var deviceReq = createRequest("devices/" + deviceId).GET().build();
			var deviceRes = sendRequest(httpClient, deviceReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8), 200);
			return objectMapper.reader().readValue(deviceRes.body(), DeviceDto.class);
		}

		public HttpResponse<Void> createOrUpdate(String deviceId, DeviceDto deviceDto) throws IOException, InterruptedException, UnexpectedStatusCodeException {
			var body = objectMapper.writeValueAsString(deviceDto);
			var deviceReq = createRequest("devices/" + deviceId).PUT(HttpRequest.BodyPublishers.ofString(body)).build();
			return sendRequest(httpClient, deviceReq, HttpResponse.BodyHandlers.discarding(), 201);
		}

	}

	class AuthorityService {

		public List<UserDto> listSome(List<String> userId) throws IOException, InterruptedException, UnexpectedStatusCodeException {
			var queryParams = String.join("&ids=", userId);
			var memberInfoReq = createRequest("authorities?ids=" + queryParams).GET().build();
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
