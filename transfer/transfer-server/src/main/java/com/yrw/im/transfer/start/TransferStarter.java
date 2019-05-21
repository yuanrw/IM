package com.yrw.im.transfer.start;

import com.yrw.im.common.domain.constant.MqConstant;

/**
 * Date: 2019-05-07
 * Time: 20:39
 *
 * @author yrw
 */
public class TransferStarter {

    public static void main(String[] args) {
        try {
            String host = "127.0.0.1";
            int mqPort = 5672;
            TransferMqProducer.startProducer(host, mqPort, MqConstant.EXCHANGE, MqConstant.OFFLINE_QUEUE, MqConstant.ROUTING_KEY);

            int port = 9082;
            TransferServer.startTransferServer(port);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
