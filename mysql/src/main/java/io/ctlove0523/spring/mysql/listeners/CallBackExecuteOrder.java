package io.ctlove0523.spring.mysql.listeners;

import javax.annotation.PostConstruct;

import io.ctlove0523.spring.mysql.listeners.inheritance.InheritanceInfoRepository;
import io.ctlove0523.spring.mysql.routes.RouteDefinition;
import io.ctlove0523.spring.mysql.routes.RouteDefinitionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CallBackExecuteOrder {

	@Autowired
	private RouteDefinitionRepository repository;

	@Autowired
	private InheritanceInfoRepository inheritanceInfoRepository;

	@PostConstruct
	public void callMethods() {
	}

	private void update() {
		RouteDefinition definition = repository.findById("402882e876861f840176861f88460000").get();
		definition.setUri("12");

		repository.save(definition);
	}

	private void delete() {
		repository.deleteByUri("http://localhost");
	}

	private void save() {
		RouteDefinition definition = new RouteDefinition();
		definition.setRouteOrder(1);
		definition.setUri("http://localhost");
		repository.save(definition);
	}
}
