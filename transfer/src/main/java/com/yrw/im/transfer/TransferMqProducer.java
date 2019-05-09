package com.yrw.im.transfer;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MQProducer;

/**
 * Date: 2019-05-06
 * Time: 14:27
 *
 * @author yrw
 */
public class RocketMqProducer {

    private static MQProducer mqProducer;

    public static void startProducer() throws MQClientException {
        DefaultMQProducer producer = new DefaultMQProducer("im");
        producer.setNamesrvAddr("localhost:9876");

        producer.start();

        producer.setRetryTimesWhenSendAsyncFailed(3);
        mqProducer = producer;
    }

    public static MQProducer getMqProducer() {
        return mqProducer;
    }
}
