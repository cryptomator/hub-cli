package org.cryptomator.hubcli;

import picocli.CommandLine;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "list-groups",
        description = "List all groups.")
class ListGroups implements Callable<Integer> {

    @CommandLine.Mixin
    Common common;

    @CommandLine.Mixin
    AccessToken accessToken;

    @Override
    public Integer call() throws InterruptedException, IOException {
        try (var httpClient = HttpClient.newHttpClient()) {
            var groupsReq = createRequest("groups").GET().build();
            var groupsRes = sendRequest(httpClient, groupsReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8), 200);
            System.out.println(groupsRes.body());
            return 0;
        } catch (UnexpectedStatusCodeException e) {
            return e.status;
        }
    }

    private HttpRequest.Builder createRequest(String path) {
        var uri = common.getApiBase().resolve(path);
        return HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(5)).header("Authorization", "Bearer " + accessToken.value);
    }

    private <T> HttpResponse<T> sendRequest(HttpClient httpClient, HttpRequest request, HttpResponse.BodyHandler<T> bodyHandler, int... expectedStatusCode) throws IOException, InterruptedException, UnexpectedStatusCodeException {
        var res = httpClient.send(request, bodyHandler);
        var status = res.statusCode();
        if (Arrays.stream(expectedStatusCode).noneMatch(s -> s == status)) {
            throw new UnexpectedStatusCodeException(status, "Unexpected response for " + request.method() + " " + request.uri() + ": " + status);
        }
        return res;
    }
}
