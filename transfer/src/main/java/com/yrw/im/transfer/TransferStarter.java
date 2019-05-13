package com.yrw.im.transfer;

/**
 * Date: 2019-05-07
 * Time: 20:39
 *
 * @author yrw
 */
public class TransferStarter {

    public static String exchange = "im";
    public static String queue = "im_offline";
    public static String routingKey = "im_offline";

    public static void main(String[] args) {
        try {
            String host = "127.0.0.1";
            int mqPort = 5672;
            TransferMqProducer.startProducer(host, mqPort, exchange, queue, routingKey);
            int port = 9082;
            TransferServer.startTransferServer(port);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
