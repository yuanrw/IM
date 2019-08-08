package com.yrw.im.client.sample;

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
public class MyClientApplication {

    public static void main(String[] args) {

        ExecutorService executorService = Executors.newFixedThreadPool(20);
        List<MyClient> myClientList = new ArrayList<>();
        String[] usernameForTest = {
            "1YzhYNFc", "2Q6GHsza", "A44UXM3B", "CLr0niCL", "GfIbDo62", "Gqbk6cD7",
            "HUYnzlMK", "MeLg2cqn", "mfjIHgak", "NSBbHtC6", "PJ8KKC3b", "SIm3mNA3",
            "t3zDGxuE", "xianyy", "yr0ds2ao", "yuanrw", "Z4gQcE1y",
        };

        //login all user
        for (String username : usernameForTest) {
            myClientList.add(new MyClient("localhost", 9081,
                "http://127.0.0.1:8082", username, "123abc"));
        }

        //print test result every 5 seconds
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        printTestResult(executor, () -> System.out.println(String.format("sentMsg: %d, readMsg: %d, hasSentAck: %d, " +
                "hasDeliveredAck: %d, hasReadAck: %d, hasException: %d",
            MyClient.sendMsg.get(), MyClient.readMsg.get(), MyClient.hasSentAck.get(),
            MyClient.hasDeliveredAck.get(), MyClient.hasReadAck.get(), MyClient.hasException.get())));

        //start simulate send
        myClientList.forEach(myClient -> executorService.submit(() -> {
                for (int i = 0; i < 1000; i++) {
                    myClient.randomSendTest();
                }
            })
        );
    }

    private static void printTestResult(ScheduledExecutorService executorService, Runnable doPrint) {
        executorService.scheduleAtFixedRate(doPrint, 0, 5, TimeUnit.SECONDS);
    }
}
