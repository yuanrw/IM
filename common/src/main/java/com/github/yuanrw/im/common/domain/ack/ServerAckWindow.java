package com.github.yuanrw.im.common.domain.ack;

import com.github.yuanrw.im.common.domain.ResponseCollector;
import com.github.yuanrw.im.common.exception.ImException;
import com.github.yuanrw.im.protobuf.generate.Internal;
import com.google.protobuf.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.time.Duration;
import java.util.Map;
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
    private static Map<Serializable, ServerAckWindow> windowsMap;
    private static ExecutorService executorService;

    private final Duration timeout;
    private final int maxSize;

    private ConcurrentHashMap<Long, ResponseCollector<Internal.InternalMsg>> responseCollectorMap;

    static {
        windowsMap = new ConcurrentHashMap<>();
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(ServerAckWindow::checkTimeoutAndRetry);
    }

    public ServerAckWindow(Serializable connectionId, int maxSize, Duration timeout) {
        this.responseCollectorMap = new ConcurrentHashMap<>();
        this.timeout = timeout;
        this.maxSize = maxSize;

        windowsMap.put(connectionId, this);
    }

    /**
     * multi thread do it
     *
     * @param id           msg id
     * @param sendMessage
     * @param sendFunction
     * @return
     */
    public static CompletableFuture<Internal.InternalMsg> offer(Serializable connectionId, Long id, Message sendMessage, Consumer<Message> sendFunction) {
        return windowsMap.get(connectionId).offer(id, sendMessage, sendFunction);
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
        responseCollector.send();
        responseCollectorMap.put(id, responseCollector);
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
    private static void checkTimeoutAndRetry() {
        while (true) {
            for (ServerAckWindow window : windowsMap.values()) {
                window.responseCollectorMap.entrySet().stream()
                    .filter(entry -> window.timeout(entry.getValue()))
                    .forEach(entry -> window.retry(entry.getKey(), entry.getValue()));
            }
        }
    }

    private void retry(Long id, ResponseCollector<?> collector) {
        logger.debug("retry msg: {}", id);
        //todo: if offline
        collector.send();
    }

    private boolean timeout(ResponseCollector<?> collector) {
        return collector.getSendTime().get() != 0 && collector.timeElapse() > timeout.toNanos();
    }
}
