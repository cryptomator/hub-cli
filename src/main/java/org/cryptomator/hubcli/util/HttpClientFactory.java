package org.cryptomator.hubcli.util;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class HttpClientFactory {

	private static final String USER_AGENT_HEADER_KEY = "User-Agent";
	private static final String USER_AGENT_HEADER_VAL = "HubCLI/" + ManifestVersionProvider.getImplementationVersion();
	public static HttpClient create() {
		var client = HttpClient.newBuilder()
				.followRedirects(HttpClient.Redirect.NORMAL)
				.proxy(ProxySelector.getDefault())
				.build();
		return new UserAgentDecoratingHttpClient(client);
	}

	private static class UserAgentDecoratingHttpClient extends HttpClientDecorator {

		public UserAgentDecoratingHttpClient(HttpClient delegate) {
			super(delegate);
		}

		@Override
		public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) throws IOException, InterruptedException {
			var modified = HttpRequest.newBuilder(request, (n, v) -> true).setHeader(USER_AGENT_HEADER_KEY, USER_AGENT_HEADER_VAL).build();
			System.out.println(modified);
			return super.send(modified, responseBodyHandler);
		}

		@Override
		public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
			var modified = HttpRequest.newBuilder(request, (n, v) -> true).setHeader(USER_AGENT_HEADER_KEY, USER_AGENT_HEADER_VAL).build();
			return super.sendAsync(modified, responseBodyHandler);
		}

		@Override
		public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler, HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
			var modified = HttpRequest.newBuilder(request, (n, v) -> true).setHeader(USER_AGENT_HEADER_KEY, USER_AGENT_HEADER_VAL).build();
			return super.sendAsync(modified, responseBodyHandler, pushPromiseHandler);
		}
	}

	private static class HttpClientDecorator extends HttpClient {

		private final HttpClient delegate;

		public HttpClientDecorator(HttpClient delegate) {
			this.delegate = delegate;
		}

		@Override
		public Optional<CookieHandler> cookieHandler() {
			return delegate.cookieHandler();
		}

		@Override
		public Optional<Duration> connectTimeout() {
			return delegate.connectTimeout();
		}

		@Override
		public Redirect followRedirects() {
			return delegate.followRedirects();
		}

		@Override
		public Optional<ProxySelector> proxy() {
			return delegate.proxy();
		}

		@Override
		public SSLContext sslContext() {
			return delegate.sslContext();
		}

		@Override
		public SSLParameters sslParameters() {
			return delegate.sslParameters();
		}

		@Override
		public Optional<Authenticator> authenticator() {
			return delegate.authenticator();
		}

		@Override
		public Version version() {
			return delegate.version();
		}

		@Override
		public Optional<Executor> executor() {
			return delegate.executor();
		}

		@Override
		public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) throws IOException, InterruptedException {
			return delegate.send(request, responseBodyHandler);
		}

		@Override
		public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
			return delegate.sendAsync(request, responseBodyHandler);
		}

		@Override
		public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler, HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
			return delegate.sendAsync(request, responseBodyHandler, pushPromiseHandler);
		}

	}

}
