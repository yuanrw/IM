package com.github.yuanrw.im.connector.start;

import com.github.yuanrw.im.common.exception.ImException;
import com.github.yuanrw.im.connector.config.ConnectorConfig;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Date: 2019-05-02
 * Time: 17:59
 *
 * @author yrw
 */
public class ConnectorStarter {
    public static ConnectorConfig CONNECTOR_CONFIG = new ConnectorConfig();

    public static void main(String[] args) throws IOException {
        //parse start parameter
        ConnectorStarter.CONNECTOR_CONFIG = parseConfig();

        //connector to transfer
        ConnectorClient.start(CONNECTOR_CONFIG.getTransferHost(), CONNECTOR_CONFIG.getTransferPort());

        //start connector server
        ConnectorServer.start(CONNECTOR_CONFIG.getPort());
    }

    private static ConnectorConfig parseConfig() throws IOException {
        Properties properties = getProperties();

        ConnectorConfig connectorConfig = new ConnectorConfig();
        connectorConfig.setPort(Integer.parseInt((String) properties.get("port")));
        connectorConfig.setTransferHost((String) properties.get("transfer.host"));
        connectorConfig.setTransferPort(Integer.parseInt((String) properties.get("transfer.port")));
        connectorConfig.setRestUrl((String) properties.get("rest.url"));
        connectorConfig.setLogPath((String) properties.get("log.path"));
        connectorConfig.setLogLevel((String) properties.get("log.level"));

        System.setProperty("log.path", connectorConfig.getLogPath());
        System.setProperty("log.level", connectorConfig.getLogLevel());

        return connectorConfig;
    }

    private static Properties getProperties() throws IOException {
        InputStream inputStream;
        String path = System.getProperty("config");
        if (path == null) {
            throw new ImException("connector.properties is not defined");
        } else {
            inputStream = new FileInputStream(path);
        }

        Properties properties = new Properties();
        properties.load(inputStream);
        return properties;
    }
}
