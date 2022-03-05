package io.github.ctlove0523.tls;

import java.util.List;

public class ServerAllowedUrl {
    private int port;
    private List<String> urls;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }
}
