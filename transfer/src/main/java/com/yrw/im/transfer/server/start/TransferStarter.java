package com.yrw.im.transfer.server.start;

import com.yrw.im.common.domain.constant.MqConstant;
import com.yrw.im.common.exception.ImException;
import com.yrw.im.transfer.server.config.TransferConfig;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Date: 2019-05-07
 * Time: 20:39
 *
 * @author yrw
 */
public class TransferStarter {
    public static TransferConfig TRANSFER_CONFIG;

    public static void main(String[] args) {
        try {
            //parse start parameter
            TransferStarter.TRANSFER_CONFIG = parseConfig();

            //start rabbitmq server
            TransferMqProducer.startProducer(TRANSFER_CONFIG.getRabbitmqHost(), TRANSFER_CONFIG.getRabbitmqPort(),
                TRANSFER_CONFIG.getRabbitmqUsername(), TRANSFER_CONFIG.getRabbitmqPassword(),
                MqConstant.EXCHANGE, MqConstant.OFFLINE_QUEUE, MqConstant.ROUTING_KEY);

            //start transfer server
            TransferServer.startTransferServer(TRANSFER_CONFIG.getPort());
        } catch (Exception e) {
            LoggerFactory.getLogger(TransferStarter.class).error("[transfer] start failed", e);
        }
    }

    private static TransferConfig parseConfig() throws IOException {
        Properties properties = getProperties();

        TransferConfig transferConfig = new TransferConfig();
        transferConfig.setPort(Integer.parseInt((String) properties.get("port")));
        transferConfig.setRedisHost((String) properties.get("redis.host"));
        transferConfig.setRedisPort(Integer.parseInt((String) properties.get("redis.port")));
        transferConfig.setRabbitmqHost((String) properties.get("rabbitmq.host"));
        transferConfig.setRabbitmqUsername((String) properties.get("rabbitmq.username"));
        transferConfig.setRabbitmqPassword((String) properties.get("rabbitmq.password"));
        transferConfig.setRabbitmqPort(Integer.parseInt((String) properties.get("rabbitmq.port")));
        transferConfig.setLogPath((String) properties.get("log.path"));
        transferConfig.setLogLevel((String) properties.get("log.level"));

        System.setProperty("log.path", transferConfig.getLogPath());
        System.setProperty("log.level", transferConfig.getLogLevel());

        return transferConfig;
    }

    private static Properties getProperties() throws IOException {
        InputStream inputStream;
        String path = System.getProperty("config");
        if (path == null) {
            throw new ImException("transfer.properties is not defined");
        } else {
            inputStream = new FileInputStream(path);
        }

        Properties properties = new Properties();
        properties.load(inputStream);
        return properties;
    }
}
