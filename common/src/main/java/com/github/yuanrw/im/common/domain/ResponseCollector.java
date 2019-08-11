package com.github.yuanrw.im.common.domain;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.protobuf.Message;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Date: 2019-05-18
 * Time: 13:50
 *
 * @author yrw
 */
public class ResponseCollector<M extends Message> {

    private CompletableFuture<M> future;

    private final Duration responseTimeout;

    private static final HashedWheelTimer TIMER = new HashedWheelTimer(new ThreadFactoryBuilder().setDaemon(true).setNameFormat("response-timer-%d").build());

    public ResponseCollector(Duration responseTimeout) {
        this.responseTimeout = responseTimeout;
        this.future = new CompletableFuture<>();
        applyResponseTimeout(future, responseTimeout);
    }

    private void applyResponseTimeout(CompletableFuture<M> responseFuture, Duration duration) {
        Duration durationTime = duration != null ? duration : responseTimeout;

        Timeout hwtTimeout = TIMER.newTimeout(ignored -> {
            String message = "Time out waiting for init response from server";

            responseFuture.completeExceptionally(new TimeoutException(message));
        }, durationTime.toMillis(), TimeUnit.MILLISECONDS);

        responseFuture.whenComplete((ignored1, ignored2) -> hwtTimeout.cancel());
    }

    public CompletableFuture<M> getFuture() {
        return future;
    }
}
