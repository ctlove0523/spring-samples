package io.ctlove0523.spring.gateway.routes;

import io.ctlove0523.spring.gateway.mysql.DbRouteDefinitionRepository;
import io.ctlove0523.spring.gateway.mysql.entities.RouteDefinitionPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * @author chentong
 */
@Component
@Slf4j
public class StorageRouteDefinitionRepository implements RouteDefinitionRepository, ApplicationEventPublisherAware {
    private ApplicationEventPublisher publisher;

    @Autowired
    private DbRouteDefinitionRepository repository;
    static {
        RouteDefinition definition = new RouteDefinition();
        definition.setId("chen-tong");
        URI uri = UriComponentsBuilder.fromHttpUrl("http://127.0.0.1:8888/header").build().toUri();
        definition.setUri(uri);

        //定义第一个断言
        PredicateDefinition predicate = new PredicateDefinition();
        predicate.setName("Path");

        Map<String, String> predicateParams = new HashMap<>(8);
        predicateParams.put("pattern", "/jd");
        predicate.setArgs(predicateParams);

        //定义Filter
        FilterDefinition filter = new FilterDefinition();
        filter.setName("AddRequestHeader");
        Map<String, String> filterParams = new HashMap<>(8);
        //该_genkey_前缀是固定的，见org.springframework.cloud.gateway.support.NameUtils类
        filterParams.put("_genkey_0", "header");
        filterParams.put("_genkey_1", "addHeader");
        filter.setArgs(filterParams);

        FilterDefinition filter1 = new FilterDefinition();
        filter1.setName("AddRequestParameter");
        Map<String, String> filter1Params = new HashMap<>(8);
        filter1Params.put("_genkey_0", "param");
        filter1Params.put("_genkey_1", "addParam");
        filter1.setArgs(filter1Params);

        definition.setFilters(Arrays.asList(filter, filter1));
        definition.setPredicates(Collections.singletonList(predicate));

        routes.put(definition.getId(), definition);
    }

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        return Flux.fromIterable(repository.findAll())
                .map(routeDefinitionPo -> {
                    RouteDefinition definition = new RouteDefinition();
                    definition.setId(routeDefinitionPo.getId());
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
            po.setId(routeDefinition.getId());
            po.setFilters(routeDefinition.getFilters());
            po.setMetadata(routeDefinition.getMetadata());
            po.setPredicates(routeDefinition.getPredicates());
            po.setRouteOrder(routeDefinition.getOrder());
            po.setUri(routeDefinition.getUri().toString());
            repository.save(po);
            return Mono.empty();
        }).doOnError(throwable -> log.error("exception ", throwable));
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return routeId.flatMap(s -> {
            repository.deleteById(s);
            return Mono.empty();
        });
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }
}
