package com.github.yuanrw.im.client.test;

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
public class TestClientApplication {

    public static void main(String[] args) {
        List<TestClient> testClientList = new ArrayList<>();
        String[] usernameForTest = {
            "Adela", "Alice", "Bella", "Cynthia", "Freda", "Honey",
            "Irene", "Iris", "Joa", "Juliet", "Lisa", "Mandy", "Nora",
            "Olive", "Tom", "xianyy", "yuanrw",
        };

        //login all user
        for (int i = 0; i < 17; i++) {
            testClientList.add(new TestClient(args[0], 9081,
                args[1], usernameForTest[i], "123abc"));
        }

        //print test result every 5 seconds
        ScheduledExecutorService printExecutor = Executors.newScheduledThreadPool(1);

        doInExecutor(printExecutor, 5, () -> {
            System.out.println("\n\n");
            System.out.println(String.format("sentMsg: %d, readMsg: %d, hasSentAck: %d, " +
                    "hasDeliveredAck: %d, hasReadAck: %d, hasException: %d",
                TestClient.sendMsg.get(), TestClient.readMsg.get(), TestClient.hasSentAck.get(),
                TestClient.hasDeliveredAck.get(), TestClient.hasReadAck.get(), TestClient.hasException.get()));
            System.out.println("\n\n");
        });


        //start simulate send
        ScheduledExecutorService clientExecutor = Executors.newScheduledThreadPool(20);

        testClientList.forEach(testClient -> doInExecutor(clientExecutor, 2, testClient::randomSendTest));
    }

    private static void doInExecutor(ScheduledExecutorService executorService, int period, Runnable doFunction) {
        executorService.scheduleAtFixedRate(doFunction, 0, period, TimeUnit.SECONDS);
    }
}