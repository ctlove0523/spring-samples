package io.github.ctlove0523.tls;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class YamlConnectorConfigureRepository implements ConnectorConfigureRepository {

    @Autowired
    private ResourceLoader resourceLoader;

    @SuppressWarnings("unchecked")
    public List<ConnectorConfigure> load() {
        List<ConnectorConfigure> connectorConfigures = new ArrayList<>();
        Yaml yaml = new Yaml();
        Resource resource = resourceLoader.getResource("classpath:application.yaml");
        try {
            Map<String, Object> applicationConfig = yaml.load(resource.getInputStream());
            List<Map<String, Object>> configs = (List<Map<String, Object>>) applicationConfig.get("servers");
            for (Map<String, Object> config : configs) {
                ConnectorConfigure connectorConfigure = createConnectorConfigure(config);
                connectorConfigures.add(connectorConfigure);
            }

            return connectorConfigures;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    private ConnectorConfigure createConnectorConfigure(Map<String, Object> config) {
        ConnectorConfigure connectorConfigure = new ConnectorConfigure();
        connectorConfigure.setScheme((String) config.get("scheme"));
        connectorConfigure.setPort((int) config.get("port"));

        if (Objects.nonNull(config.get("sslEnabled"))) {
            connectorConfigure.setSslEnabled((boolean) config.get("sslEnabled"));
        }
        if (Objects.nonNull(config.get("keystoreFile"))) {
            connectorConfigure.setKeystoreFile((String) config.get("keystoreFile"));
        }
        if (Objects.nonNull(config.get("keystorePass"))) {
            connectorConfigure.setKeystorePass(config.get("keystorePass").toString());
        }

        if (Objects.nonNull(config.get("keyAlias"))) {
            connectorConfigure.setKeyAlias((String) config.get("keyAlias"));
        }

        if (Objects.nonNull(config.get("allowedUrls"))) {
            connectorConfigure.setAllowedUrls((List<String>) config.get("allowedUrls"));
        }

        return connectorConfigure;
    }
}
