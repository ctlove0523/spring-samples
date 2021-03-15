package io.ctlove0523.spring.gateway.routes;

import io.ctlove0523.spring.gateway.mysql.DbRouteDefinitionRepository;
import io.ctlove0523.spring.gateway.mysql.entities.RouteDefinitionPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

/**
 * @author chentong
 */
//@Component
@Slf4j
public class StorageRouteDefinitionRepository implements RouteDefinitionRepository, ApplicationEventPublisherAware {
    private ApplicationEventPublisher publisher;

    @Autowired
    private DbRouteDefinitionRepository repository;

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        List<RouteDefinitionPO> routeDefinitionPOS = repository.findAll();
        if (routeDefinitionPOS.isEmpty()) {
            return Flux.empty();
        }

        return Flux.fromIterable(routeDefinitionPOS)
                .map(routeDefinitionPo -> {
                    RouteDefinition definition = new RouteDefinition();
                    definition.setId(routeDefinitionPo.getRouteId());
                    definition.setFilters(routeDefinitionPo.getFilters());
                    definition.setMetadata(routeDefinitionPo.getMetadata());
                    definition.setOrder(routeDefinitionPo.getRouteOrder());
                    definition.setPredicates(routeDefinitionPo.getPredicates());
                    definition.setUri(UriComponentsBuilder.fromUriString(routeDefinitionPo.getUri()).build().toUri());
                    return definition;
                }).onErrorStop();
    }

    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        return route.flatMap((Function<RouteDefinition, Mono<? extends Void>>) routeDefinition -> {
            RouteDefinitionPO po = new RouteDefinitionPO();
            po.setRouteId(routeDefinition.getId());
            po.setFilters(routeDefinition.getFilters());
            po.setMetadata(routeDefinition.getMetadata());
            po.setPredicates(routeDefinition.getPredicates());
            po.setRouteOrder(routeDefinition.getOrder());
            po.setUri(routeDefinition.getUri().toString());
            repository.save(po);

            publisher.publishEvent(new RefreshRoutesEvent(this));
            return Mono.empty();
        }).doOnError(throwable -> log.error("exception ", throwable));
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return routeId.flatMap(s -> {
            repository.deleteByRouteId(s);
            publisher.publishEvent(new RefreshRoutesEvent(this));
            return Mono.empty();
        });
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }
}
