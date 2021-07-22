package io.ctlove0523.spring.gateway.config;

import java.util.function.Function;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.config.MeterFilter;
import reactor.netty.http.server.HttpServer;

import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@Component
public class ServerConfigure implements WebServerFactoryCustomizer<NettyReactiveWebServerFactory> {


	@Override
	public void customize(NettyReactiveWebServerFactory factory) {
		Metrics.globalRegistry
				.config()
				.meterFilter(MeterFilter.maximumAllowableTags("reactor.netty.http.server", "URI", 100, MeterFilter.deny()));

		factory.addServerCustomizers(new NettyServerCustomizer() {
			@Override
			public HttpServer apply(HttpServer httpServer) {
				return httpServer.metrics(true, new Function<String, String>() {
					@Override
					public String apply(String s) {
						return s;
					}
				});
			}
		});
	}
}
