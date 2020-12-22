package io.ctlove0523.spring.mysql.routes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author chentong
 */
@Controller
public class RouteDefinitionController {
	private static final Logger log = LoggerFactory.getLogger(RouteDefinitionController.class);

	@Autowired
	private RouteDefinitionRepository routeDefinitionRepository;

	@RequestMapping(value = "/v5/api/route-definition", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RouteDefinition> createRouteDefinition(@RequestBody RouteDefinition routeDefinition) {
		log.info("begin to create new route definition");

		routeDefinitionRepository.save(routeDefinition);
		return new ResponseEntity<>(routeDefinition, HttpStatus.ACCEPTED);

	}
}
