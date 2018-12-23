package io.github.ctlove0523.circuitbreakersclient.weather;

/**
 * Copyright (C), 2018-2018, open source
 * FileName: WeatherInfo
 *
 * @author: chentong
 * Date:     2018/12/23 13:29
 */
public class WeatherInfo {
    private double temperature;
    private double humidity;
    private String type;

    public WeatherInfo() {

    }

    public WeatherInfo(double temperature, double humidity, String type) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.type = type;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
