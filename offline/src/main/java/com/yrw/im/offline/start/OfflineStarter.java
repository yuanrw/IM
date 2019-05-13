package com.yrw.im.offline.start;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.rabbitmq.client.*;
import com.yrw.im.offline.config.MybatisConfig;
import com.yrw.im.offline.config.OfflineModule;
import com.yrw.im.offline.consumer.OfflineConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Date: 2019-05-05
 * Time: 09:46
 *
 * @author yrw
 */
public class OfflineStarter {
    private static Logger logger = LoggerFactory.getLogger(OfflineStarter.class);

    static Injector injector = Guice.createInjector(new OfflineModule());
    public static Channel channel;

    public static void main(String[] args) {
        try {
            String host = "127.0.0.1";
            int port = 5672;
            String exchange = "im";
            String queue = "im_offline";

            MybatisConfig.setMybatisConfig();
            startMqConsumer(host, port, exchange, queue);

            logger.info("[OfflineStarter] Offline start success");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void startMqConsumer(String host, int port, String exchange, String queue) throws IOException, TimeoutException {
        OfflineConsumer offlineConsumer = injector.getInstance(OfflineConsumer.class);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        final Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();
        channel.exchangeDeclare(exchange, BuiltinExchangeType.DIRECT, true, false, null);
        channel.queueDeclare(queue, true, false, false, null);

        channel.basicQos(1);

        OfflineStarter.channel = channel;

        DeliverCallback deliverCallback = offlineConsumer::doConsumer;

        channel.basicConsume(queue, false, deliverCallback, consumerTag -> { });

        logger.info("[OfflineStarter] consumer start success");
    }

    public static Channel getChannel() {
        return channel;
    }
}
