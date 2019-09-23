package com.github.yuanrw.im.common.domain.ack;

import com.github.yuanrw.im.common.domain.ResponseCollector;
import com.github.yuanrw.im.common.exception.ImException;
import com.github.yuanrw.im.protobuf.generate.Internal;
import com.google.protobuf.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private final Duration timeout;
    private final int maxSize;

    private ConcurrentHashMap<Long, ResponseCollector<Internal.InternalMsg>> responseCollectorMap;

    public ServerAckWindow(int maxSize, Duration timeout) {
        this.responseCollectorMap = new ConcurrentHashMap<>();
        this.timeout = timeout;
        this.maxSize = maxSize;

        executor.submit(this::checkTimeoutAndRetry);
    }

    /**
     * multi thread do it
     *
     * @param id           msg id
     * @param sendMessage
     * @param sendFunction
     * @return
     */
    public CompletableFuture<Internal.InternalMsg> offer(Long id, Message sendMessage, Consumer<Message> sendFunction) {
        if (responseCollectorMap.containsKey(id)) {
            CompletableFuture<Internal.InternalMsg> future = new CompletableFuture<>();
            future.completeExceptionally(new ImException("send repeat msg id: " + id));
            return future;
        }
        if (responseCollectorMap.size() >= maxSize) {
            CompletableFuture<Internal.InternalMsg> future = new CompletableFuture<>();
            future.completeExceptionally(new ImException("server window is full"));
            return future;
        }

        ResponseCollector<Internal.InternalMsg> responseCollector = new ResponseCollector<>(sendMessage, sendFunction);
        responseCollectorMap.put(id, responseCollector);
        responseCollector.send();
        return responseCollector.getFuture();
    }

    public void ack(Internal.InternalMsg message) {
        Long id = Long.parseLong(message.getMsgBody());
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
            for (Long id : responseCollectorMap.keySet()) {
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
            logger.debug("send msg {}", id);
            collector.send();
        }
    }

    private boolean timeout(ResponseCollector<?> collector) {
        return collector.getSendTime().get() != 0 && collector.timeElapse() > timeout.getNano();
    }

    private boolean canRetry(ResponseCollector<?> collector) {
        return collector.getSending().compareAndSet(false, true);
    }
}
