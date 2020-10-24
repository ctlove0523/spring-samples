package io.ctlove0523.spring.gateway.routes;

import io.ctlove0523.spring.gateway.mysql.DbRouteDefinitionRepository;
import io.ctlove0523.spring.gateway.mysql.entities.RouteDefinitionPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.Function;

/**
 * @author chentong
 */
@RestControllerEndpoint(id = "self-gateway")
@Slf4j
public class SelfGatewayControllerEndpoint implements ApplicationEventPublisherAware {
    private ApplicationEventPublisher publisher;

    @Autowired
    private DbRouteDefinitionRepository repository;

    @PutMapping("/routes/{id}")
    public Mono<ResponseEntity<Object>> modifyRoute(@PathVariable String id,
                                                    @RequestBody RouteDefinition routeDefinition) {
        log.info("begin to update route with id {}", id);
        return Mono.just(id).flatMap(new Function<String, Mono<?>>() {
            @Override
            public Mono<?> apply(String s) {
                Optional<RouteDefinitionPO> routeDefinitionPO = repository.findByRouteId(s);
                if (routeDefinitionPO.isPresent()) {
                    if (routeDefinition.getFilters() != null) {
                        routeDefinitionPO.get().setFilters(routeDefinition.getFilters());
                    }
                    if (routeDefinition.getMetadata() != null) {
                        routeDefinitionPO.get().setMetadata(routeDefinition.getMetadata());
                    }
                    if (routeDefinition.getOrder() != 0) {
                        routeDefinitionPO.get().setRouteOrder(routeDefinition.getOrder());
                    }
                    if (routeDefinition.getPredicates() != null) {
                        routeDefinitionPO.get().setPredicates(routeDefinition.getPredicates());
                    }
                    if (routeDefinition.getUri() != null) {
                        routeDefinitionPO.get().setUri(routeDefinition.getUri().toString());
                    }
                    repository.save(routeDefinitionPO.get());
                    return Mono.empty();
                }
                return Mono.defer(() -> Mono.error(
                        new NotFoundException("RouteDefinition not found: " + id)));
            }

            ;
        }).then(Mono.defer(() -> {
            publisher.publishEvent(new RefreshRoutesEvent(this));
            return Mono.just(ResponseEntity.ok().build());
        }))
                .onErrorResume((Throwable t) -> t instanceof NotFoundException,
                        t -> Mono.just(ResponseEntity.notFound().build()));


    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }
}
