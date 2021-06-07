package io.ctlove0523.spring.gateway.health;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class TestController {

	@RequestMapping(value = "/api/lb",method = RequestMethod.GET)
	public ResponseEntity<String> lb() {
		return new ResponseEntity<>("lb", HttpStatus.OK);
	}

	@RequestMapping(value = "/api/no-lb",method = RequestMethod.GET)
	public ResponseEntity<String> noLb() {
		return new ResponseEntity<>("no-lb", HttpStatus.OK);
	}
}
