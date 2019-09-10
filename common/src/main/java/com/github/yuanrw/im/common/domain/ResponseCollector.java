package com.github.yuanrw.im.common.domain;

import com.google.protobuf.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * thread safe
 * Date: 2019-05-18
 * Time: 13:50
 *
 * @author yrw
 */
public class ResponseCollector<M extends Message> {
    private static Logger logger = LoggerFactory.getLogger(ResponseCollector.class);

    private Message sendMessage;
    private Consumer<Message> sendFunction;
    private CompletableFuture<M> future;

    private volatile AtomicLong sendTime;
    private volatile AtomicBoolean retrying;

    public ResponseCollector(Message sendMessage, Consumer<Message> sendFunction) {
        this.sendMessage = sendMessage;
        this.sendFunction = sendFunction;
        this.future = new CompletableFuture<>();
        this.sendTime = new AtomicLong(0);
        this.retrying = new AtomicBoolean(false);
    }

    public void retry() {
        this.sendTime.set(System.currentTimeMillis());
        try {
            sendFunction.accept(sendMessage);
        } catch (Exception e) {
            logger.error("send msg retry has error", e);
        } finally {
            this.retrying.set(false);
        }
    }

    public CompletableFuture<M> getFuture() {
        return future;
    }

    public AtomicLong getSendTime() {
        return sendTime;
    }

    public AtomicBoolean getRetrying() {
        return retrying;
    }
}