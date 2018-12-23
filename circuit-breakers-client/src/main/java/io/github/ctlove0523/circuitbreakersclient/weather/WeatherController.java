package io.github.ctlove0523.circuitbreakersclient.weather;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Copyright (C), 2018-2018, open source
 * FileName: WeatherClientController
 *
 * @author: chentong
 * Date:     2018/12/23 18:35
 */
@Controller
public class WeatherController {
    private static Logger log = LoggerFactory.getLogger(WeatherController.class);

    @Autowired
    private WeatherService weatherService;

    @RequestMapping(value = "/api/v1.0/weather",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WeatherInfo> getWeatherInfo() throws Exception {
        log.info("begin to get weather info");
        return new ResponseEntity<>(weatherService.getWeatherInfo(),HttpStatus.OK);
    }

    @RequestMapping(value = "/api/v1.1/weather",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WeatherInfo> queryWeatherInfo() throws Exception {
        log.info("begin to get weather info");
        return new ResponseEntity<>(weatherService.queryWeatherInfo(),HttpStatus.OK);
    }


}
