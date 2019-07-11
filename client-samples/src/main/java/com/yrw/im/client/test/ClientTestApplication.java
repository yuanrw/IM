package com.yrw.im.client.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Date: 2019-05-15
 * Time: 13:57
 *
 * @author yrw
 */
public class ClientTestApplication {

    public static void main(String[] args) {

        ExecutorService executorService = Executors.newFixedThreadPool(20);
        List<ClientTest> clientTestList = new ArrayList<>();
        String[] usernameForTest = {
            "1YzhYNFc", "2Q6GHsza", "A44UXM3B", "CLr0niCL", "GfIbDo62", "Gqbk6cD7",
            "HUYnzlMK", "MeLg2cqn", "mfjIHgak", "NSBbHtC6", "PJ8KKC3b", "SIm3mNA3",
            "t3zDGxuE", "xianyy", "yr0ds2ao", "yuanrw", "Z4gQcE1y",
        };

        //login all user
        for (String username : usernameForTest) {
            clientTestList.add(new ClientTest("localhost", 9081, username, "123abc"));
        }

        //print test result per second
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        printTestResult(executor, () -> System.out.println(String.format("sentMsg: %d, readMsg: %d, hasSentAck: %d, " +
                "hasDeliveredAck: %d, hasReadAck: %d, hasException: %d",
            ClientTest.sendMsg.get(), ClientTest.readMsg.get(), ClientTest.hasSentAck.get(),
            ClientTest.hasDeliveredAck.get(), ClientTest.hasReadAck.get(), ClientTest.hasException.get())));

        //start test
        clientTestList.forEach(clientTest -> executorService.submit(() -> {
                for (int i = 0; i < 1000; i++) {
                    clientTest.randomSendTest();
                }
            })
        );
    }

    private static void printTestResult(ScheduledExecutorService executorService, Runnable doPrint) {
        executorService.scheduleAtFixedRate(doPrint, 0, 1, TimeUnit.SECONDS);
    }
}
