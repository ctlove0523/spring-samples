package io.github.ctlove0523.nacos.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class NacosController {

	@RequestMapping(value = "/check",method = RequestMethod.GET)
	public ResponseEntity<Void> check() {
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
