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

    private static final HashedWheelTimer TIMER = new HashedWheelTimer(new ThreadFactoryBuilder().setDaemon(true).setNameFormat("response-timer-%d").build());
    private final Duration responseTimeout;

    private final String debugMsg;
    private CompletableFuture<M> future;

    public ResponseCollector(Duration responseTimeout, String debugMsg) {
        this.responseTimeout = responseTimeout;
        this.future = new CompletableFuture<>();
        this.debugMsg = debugMsg;
        applyResponseTimeout(future, responseTimeout);
    }

    private void applyResponseTimeout(CompletableFuture<M> responseFuture, Duration duration) {
        Duration durationTime = duration != null ? duration : responseTimeout;

        Timeout hwtTimeout = TIMER.newTimeout(ignored ->
                responseFuture.completeExceptionally(new TimeoutException(debugMsg))
            , durationTime.toMillis(), TimeUnit.MILLISECONDS);

        responseFuture.whenComplete((ignored1, ignored2) -> hwtTimeout.cancel());
    }

    public CompletableFuture<M> getFuture() {
        return future;
    }
}
