package com.github.yuanrw.im.transfer.start;

import com.github.yuanrw.im.common.domain.constant.ImConstant;
import com.github.yuanrw.im.protobuf.constant.MsgTypeEnum;
import com.google.inject.Singleton;
import com.google.protobuf.Message;
import com.rabbitmq.client.*;
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
@Singleton
public class TransferMqProducer {
    private static Logger logger = LoggerFactory.getLogger(TransferMqProducer.class);

    private Channel channel;

    public TransferMqProducer(String host, int port, String username, String password)
        throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(ImConstant.MQ_EXCHANGE, BuiltinExchangeType.DIRECT, true, false, null);
        channel.queueDeclare(ImConstant.MQ_OFFLINE_QUEUE, true, false, false, null);
        channel.queueBind(ImConstant.MQ_OFFLINE_QUEUE, ImConstant.MQ_EXCHANGE, ImConstant.MQ_ROUTING_KEY);

        this.channel = channel;
        logger.info("[transfer] producer start success");
    }

    public void basicPublish(String exchange, String routingKey, AMQP.BasicProperties properties, Message message) throws IOException {
        int code = MsgTypeEnum.getByClass(message.getClass()).getCode();

        byte[] srcB = message.toByteArray();
        byte[] destB = new byte[srcB.length + 1];
        destB[0] = (byte) code;

        System.arraycopy(message.toByteArray(), 0, destB, 1, message.toByteArray().length);

        channel.basicPublish(exchange, routingKey, properties, destB);
    }

    public Channel getChannel() {
        return channel;
    }
}
