package io.ctlove0523.spring.gateway.dispatcher;

import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import org.springframework.web.reactive.DispatcherHandler;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebHandler;

public class SelfDispatcherHandler implements WebHandler {
	private DispatcherHandler delegate;

	SelfDispatcherHandler(DispatcherHandler delegate) {
		this.delegate = delegate;
	}

	@Override
	public Mono<Void> handle(@NotNull ServerWebExchange serverWebExchange) {
		String path = serverWebExchange.getRequest().getPath().value();
		// 兼容处理
		path = path.replaceAll("//", "/");

		ServerWebExchange exchange = serverWebExchange.mutate()
				.request(serverWebExchange.getRequest()
						.mutate()
						.path(path).build())
				.build();
		return delegate.handle(exchange);
	}

}
