package com.yrw.im.connector.start;

import com.yrw.im.common.exception.ImException;
import com.yrw.im.connector.domain.ConnectorConfig;

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

        //start connector server
        ConnectorServer.start(CONNECTOR_CONFIG.getPort());

        //connector to transfer
        ConnectorClient.start(CONNECTOR_CONFIG.getTransferHost(), CONNECTOR_CONFIG.getTransferPort());
    }

    private static ConnectorConfig parseConfig() throws IOException {
        Properties properties = getProperties();

        ConnectorConfig transferConfig = new ConnectorConfig();
        transferConfig.setPort(Integer.parseInt((String) properties.get("port")));
        transferConfig.setTransferHost((String) properties.get("transfer.host"));
        transferConfig.setTransferPort(Integer.parseInt((String) properties.get("transfer.port")));
        transferConfig.setLogPath((String) properties.get("log.path"));

        System.setProperty("log.path", transferConfig.getLogPath());

        return transferConfig;
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
