package io.github.ctlove0523.jwt.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.github.ctlove0523.jwt.controller.dto.Application;
import io.github.ctlove0523.jwt.controller.dto.CreateApplicationRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author chentong
 */
@Controller
public class ApplicationController {
	private Map<String, Application> applications = new HashMap<>();

	@RequestMapping(value = "/v1/applications", method = RequestMethod.POST)
	public ResponseEntity<Application> createApplication(@RequestBody CreateApplicationRequest request) {
		Application app = new Application();
		app.setId(UUID.randomUUID().toString());
		app.setName(request.getName());
		app.setDescription(request.getDescription());
		applications.put(app.getId(), app);

		return new ResponseEntity<>(app, HttpStatus.OK);
	}

	@RequestMapping(value = "/v1/applications", method = RequestMethod.GET)
	public ResponseEntity<List<Application>> listApplication() {
		List<Application> apps = new ArrayList<>(applications.values());
		return new ResponseEntity<>(apps, HttpStatus.OK);
	}
}
