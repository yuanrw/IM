package com.github.yuanrw.im.transfer.start;

import com.github.yuanrw.im.common.exception.ImException;
import com.github.yuanrw.im.transfer.config.TransferConfig;
import com.github.yuanrw.im.transfer.config.TransferModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
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
    public static TransferConfig TRANSFER_CONFIG = new TransferConfig();
    public static TransferMqProducer producer;
    static Injector injector = Guice.createInjector(new TransferModule());

    public static void main(String[] args) {
        try {
            //parse start parameter
            TransferStarter.TRANSFER_CONFIG = parseConfig();

            //start rabbitmq server
            producer = new TransferMqProducer(TRANSFER_CONFIG.getRabbitmqHost(), TRANSFER_CONFIG.getRabbitmqPort(),
                TRANSFER_CONFIG.getRabbitmqUsername(), TRANSFER_CONFIG.getRabbitmqPassword());

            //start transfer server
            TransferServer.startTransferServer(TRANSFER_CONFIG.getPort());
        } catch (Exception e) {
            LoggerFactory.getLogger(TransferStarter.class).error("[transfer] start failed", e);
        }
    }

    private static TransferConfig parseConfig() throws IOException {
        Properties properties = getProperties();

        TransferConfig transferConfig = new TransferConfig();
        try {
            transferConfig.setPort(Integer.parseInt((String) properties.get("port")));
            transferConfig.setRedisHost(properties.getProperty("redis.host"));
            transferConfig.setRedisPort(Integer.parseInt(properties.getProperty("redis.port")));
            transferConfig.setRedisPassword(properties.getProperty("redis.password"));
            transferConfig.setRabbitmqHost(properties.getProperty("rabbitmq.host"));
            transferConfig.setRabbitmqUsername(properties.getProperty("rabbitmq.username"));
            transferConfig.setRabbitmqPassword(properties.getProperty("rabbitmq.password"));
            transferConfig.setRabbitmqPort(Integer.parseInt(properties.getProperty("rabbitmq.port")));
        } catch (Exception e) {
            throw new ImException("there's a parse error, check your config properties");
        }

        System.setProperty("log.path", properties.getProperty("log.path"));
        System.setProperty("log.level", properties.getProperty("log.level"));

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
