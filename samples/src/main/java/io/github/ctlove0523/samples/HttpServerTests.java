package io.github.ctlove0523.samples;

import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpRouteHandler;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;

public class HttpServerTests {
	public static void main(String[] args) {
		HttpServer.create()
				.port(3345)
				.host("127.0.0.1")
				.route(new Consumer<HttpServerRoutes>() {
					@Override
					public void accept(HttpServerRoutes httpServerRoutes) {
						httpServerRoutes
								.get("/yes/{value}", new BiFunction<HttpServerRequest, HttpServerResponse, Publisher<Void>>() {
									@Override
									public Publisher<Void> apply(HttpServerRequest httpServerRequest, HttpServerResponse httpServerResponse) {
										System.out.println("{value}");
										return httpServerResponse.sendString(Mono.just("{value}"));
									}
								})
								.get("/yes/value", new BiFunction<HttpServerRequest, HttpServerResponse, Publisher<Void>>() {
									@Override
									public Publisher<Void> apply(HttpServerRequest httpServerRequest, HttpServerResponse httpServerResponse) {
										System.out.println("value");
										return Mono.empty();
									}
								}).comparator(new Supplier<Comparator<HttpRouteHandler>>() {
							@Override
							public Comparator<HttpRouteHandler> get() {
								return new Comparator<HttpRouteHandler>() {
									@Override
									public int compare(HttpRouteHandler o1, HttpRouteHandler o2) {
										return o1.getPath().compareTo(o2.getPath());
									}
								};
							}
						});
					}
				})
				.bindNow().onDispose().block();
	}
}
