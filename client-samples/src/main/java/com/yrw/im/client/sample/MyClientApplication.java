package com.yrw.im.client.sample;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Date: 2019-05-15
 * Time: 13:57
 *
 * @author yrw
 */
public class MyClientApplication {

    public static void main(String[] args) {

        List<MyClient> myClientList = new ArrayList<>();
        String[] usernameForTest = {
            "Adela", "Alice", "Bella", "Cynthia", "Freda", "Honey",
            "Irene", "Iris", "Joa", "Juliet", "Lisa", "Mandy", "Nora",
            "Olive", "Tom", "xianyy", "yuanrw",
        };

        //login all user
        for (String username : usernameForTest) {
            myClientList.add(new MyClient(args[0], 9081,
                args[1], username, "123abc"));
        }

        //print test result every 5 seconds
        ScheduledExecutorService printExecutor = Executors.newScheduledThreadPool(1);

        doInExecutor(printExecutor, 5, () -> {
            System.out.println("\n\n");
            System.out.println(String.format("sentMsg: %d, readMsg: %d, hasSentAck: %d, " +
                    "hasDeliveredAck: %d, hasReadAck: %d, hasException: %d",
                MyClient.sendMsg.get(), MyClient.readMsg.get(), MyClient.hasSentAck.get(),
                MyClient.hasDeliveredAck.get(), MyClient.hasReadAck.get(), MyClient.hasException.get()));
            System.out.println("\n\n");
        });


        //start simulate send
        ScheduledExecutorService clientExecutor = Executors.newScheduledThreadPool(20);

        myClientList.forEach(myClient -> doInExecutor(clientExecutor, 2, myClient::randomSendTest));
    }

    private static void doInExecutor(ScheduledExecutorService executorService, int period, Runnable doFunction) {
        executorService.scheduleAtFixedRate(doFunction, 0, period, TimeUnit.SECONDS);
    }
}