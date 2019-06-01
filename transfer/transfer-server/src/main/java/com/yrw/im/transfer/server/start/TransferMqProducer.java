package com.yrw.im.transfer.server.start;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Date: 2019-05-06
 * Time: 14:27
 *
 * @author yrw
 */
public class TransferMqProducer {
    private static Logger logger = LoggerFactory.getLogger(TransferMqProducer.class);

    private static Channel channel;

    public static void startProducer(String host, int port, String exchange, String queue, String routingKey) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(exchange, BuiltinExchangeType.DIRECT, true, false, null);
        channel.queueDeclare(queue, true, false, false, null);
        channel.queueBind(queue, exchange, routingKey);

        TransferMqProducer.channel = channel;
        logger.info("[transfer] producer start success");
    }

    public static Channel getChannel() {
        return channel;
    }
}
