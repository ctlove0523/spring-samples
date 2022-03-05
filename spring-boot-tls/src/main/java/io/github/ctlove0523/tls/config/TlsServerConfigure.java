package io.github.ctlove0523.tls.config;

import io.github.ctlove0523.tls.ConnectorConfigure;
import io.github.ctlove0523.tls.ConnectorConfigureRepository;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

@Component
public class TlsServerConfigure implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    @Autowired
    private ConnectorConfigureRepository configureRepository;

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        Connector[] connectors = createConnectors();
        TomcatConnectorCustomizer customizer = new TomcatConnectorCustomizer() {
            @Override
            public void customize(Connector connector) {
                Connector copy = connectors[0];
                connector.setPort(copy.getPort());
                connector.setScheme(copy.getScheme());

                Http11NioProtocol handler = (Http11NioProtocol)connector.getProtocolHandler();
                Http11NioProtocol copyHandler = (Http11NioProtocol)copy.getProtocolHandler();
                handler.setSSLEnabled(copyHandler.isSSLEnabled());
                handler.setKeystoreFile(copyHandler.getKeystoreFile());
                handler.setKeystorePass(copyHandler.getKeystorePass());
                handler.setKeyAlias(copyHandler.getKeyAlias());
            }
        };
        factory.addConnectorCustomizers(customizer);
        // used for health check
        for (int i = 1; i < connectors.length; i++) {
            factory.addAdditionalTomcatConnectors(connectors[i]);
        }
    }

    private Connector[] createConnectors() {
        List<ConnectorConfigure> configures = configureRepository.load();
        if (configures.isEmpty()) {
            return new Connector[0];
        }

        Connector[] connectors = new Connector[configures.size()];
        for (int i = 0; i < configures.size(); i++) {
            Connector connector = new Connector();
            connector.setPort(configures.get(i).getPort());
            connector.setScheme(configures.get(i).getScheme());
            if (configures.get(i).isSslEnabled()) {
                Http11NioProtocol handler = (Http11NioProtocol) connector.getProtocolHandler();
                handler.setSSLEnabled(true);
                handler.setKeystoreFile(configures.get(i).getKeystoreFile());
                handler.setKeystorePass(configures.get(i).getKeystorePass());
                handler.setKeyAlias(configures.get(i).getKeyAlias());
            }
            connectors[i] = connector;
        }

        return connectors;
    }

    private Connector createHttpConnector() {
        Connector connector = new Connector();
        connector.setPort(8080);
        connector.setScheme("http");
        return connector;
    }

    private InetAddress getBindHost() {
        try {
            return Inet4Address.getByName("localhost");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getKeystoreFile() {
        return "D:\\codes\\GitHub\\spring-samples\\spring-boot-tls\\src\\main\\resources\\keystore.p12";
    }
}
