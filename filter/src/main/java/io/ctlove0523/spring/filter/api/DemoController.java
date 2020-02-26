package io.ctlove0523.spring.filter.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.concurrent.TimeUnit;

/**
 * @author chentong
 */
@Controller
public class DemoController {

    @RequestMapping(value = "/api/filter", method = RequestMethod.GET)
    public ResponseEntity<Void> testFilter() throws Exception {
        // simulated business
        TimeUnit.MILLISECONDS.sleep(500);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
