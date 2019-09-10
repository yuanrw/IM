package com.github.yuanrw.im.common.domain.ack;

import com.github.yuanrw.im.common.domain.ResponseCollector;
import com.github.yuanrw.im.common.exception.ImException;
import com.github.yuanrw.im.protobuf.generate.Internal;
import com.google.protobuf.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Iterator;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * for server, every connection should has an ServerAckWindow
 * Date: 2019-09-08
 * Time: 12:36
 *
 * @author yrw
 */
public class ServerAckWindow {
    private static Logger logger = LoggerFactory.getLogger(ServerAckWindow.class);

    private AtomicBoolean offer;
    private ConcurrentLinkedQueue<Long> sendMessageQueue;
    private ConcurrentHashMap<Long, ResponseCollector<Internal.InternalMsg>> responseCollectorMap;

    private final int maxSize;
    private final Duration timeout;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public ServerAckWindow(int maxSize, Duration timeout) {
        this.offer = new AtomicBoolean(false);
        this.sendMessageQueue = new ConcurrentLinkedQueue<>();
        this.responseCollectorMap = new ConcurrentHashMap<>(maxSize);
        this.maxSize = maxSize;
        this.timeout = timeout;

        executor.submit(this::checkTimeoutAndRetry);
    }

    /**
     * multi thread do it, it will try forever until success or the queue is full.
     *
     * @param id           msg id
     * @param sendMessage
     * @param sendFunction
     * @return
     */
    public CompletableFuture<Internal.InternalMsg> offer(Long id, Message sendMessage, Consumer<Message> sendFunction) {
        while (!offer.compareAndSet(false, true)) {
        }
        ResponseCollector<Internal.InternalMsg> responseCollector = new ResponseCollector<>(sendMessage, sendFunction);
        if (sendMessageQueue.size() < maxSize) {
            responseCollectorMap.put(id, responseCollector);
            sendMessageQueue.offer(id);
            sendFunction.accept(sendMessage);
            responseCollector.getSendTime().set(System.currentTimeMillis());
        } else {
            responseCollector.getFuture().completeExceptionally(new ImException("the queue is full"));
        }
        offer.set(false);
        return responseCollector.getFuture();
    }

    public void ack(Internal.InternalMsg message) {
        Long id = Long.parseLong(message.getMsgBody());
        sendMessageQueue.remove(id);
        logger.debug("get ack, msg: {}", id);
        if (responseCollectorMap.containsKey(id)) {
            responseCollectorMap.get(id).getFuture().complete(message);
            responseCollectorMap.remove(id);
        }
    }

    /**
     * single thread do it
     */
    private void checkTimeoutAndRetry() {
        while (true) {
            Iterator<Long> iterator = sendMessageQueue.iterator();
            while (iterator.hasNext()) {
                Long id = iterator.next();
                ResponseCollector<?> collector = responseCollectorMap.get(id);
                if (timeout(collector)) {
                    logger.debug("msg {} is timeout", id);
                    retry(id, collector);
                }
            }
        }
    }

    private void retry(Long id, ResponseCollector<?> collector) {
        if (canRetry(collector)) {
            logger.debug("retry msg {}", id);
            collector.retry();
        }
    }

    private boolean timeout(ResponseCollector<?> collector) {
        return collector.getSendTime().get() != 0 &&
            System.currentTimeMillis() - collector.getSendTime().get() > timeout.getNano() / 1000000;
    }

    private boolean canRetry(ResponseCollector<?> collector) {
        return collector.getRetrying().compareAndSet(false, true);
    }
}