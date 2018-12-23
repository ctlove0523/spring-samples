package io.github.ctlove0523.circuitbreakersserver.weather;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Random;

/**
 * Copyright (C), 2018-2018, open source
 * FileName: WeatherController
 *
 * @author: chentong
 * Date:     2018/12/23 13:31
 */
@Controller
public class WeatherController {
    private static final Random random = new Random();

    @RequestMapping(value = "/api/v1.0/weather", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WeatherInfo> getWeatherInfo() throws Exception {
        WeatherInfo weatherInfo = new WeatherInfo();
        weatherInfo.setType("sunshine");
        weatherInfo.setHumidity(random.nextDouble());
        weatherInfo.setTemperature(random.nextDouble());
        return new ResponseEntity<>(weatherInfo, HttpStatus.OK);
    }
}
