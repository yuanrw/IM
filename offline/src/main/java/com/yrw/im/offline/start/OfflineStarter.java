package com.yrw.im.offline;

import org.apache.rocketmq.client.exception.MQClientException;

/**
 * Date: 2019-05-05
 * Time: 09:46
 *
 * @author yrw
 */
public class OfflineStarter {

    public static void main(String[] args) {
        String host = "172.17.0.3";
        int port = 10911;
        String topic = "im";
        String subExpression = "*";

        try {
            OfflineConsumerStarter.startMqConsumer(host, port, topic, subExpression);
        } catch (MQClientException e) {
            e.printStackTrace();
        }
    }

}
