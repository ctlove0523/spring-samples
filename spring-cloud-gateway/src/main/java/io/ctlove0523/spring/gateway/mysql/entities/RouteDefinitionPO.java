package io.ctlove0523.spring.gateway.mysql.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import io.ctlove0523.spring.gateway.mysql.converters.FilterConverter;
import io.ctlove0523.spring.gateway.mysql.converters.MapConverter;
import io.ctlove0523.spring.gateway.mysql.converters.PredicatesConverter;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;

/**
 * @author chentong
 */
@Getter
@Setter
@Entity
@Table(name = "t_route_definition")
public class RouteDefinitionPO {
    @Id
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid")
    private String id;

    private String routeId;

    @Convert(converter = PredicatesConverter.class)
    private List<PredicateDefinition> predicates;

    @Convert(converter = FilterConverter.class)
    private List<FilterDefinition> filters = new ArrayList<>();

    private String uri;

    @Convert(converter = MapConverter.class)
    private Map<String,Object> metadata;

    private int routeOrder;
}
