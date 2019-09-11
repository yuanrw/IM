package com.github.yuanrw.im.common.domain.ack;

import com.github.yuanrw.im.common.domain.constant.MsgVersion;
import com.github.yuanrw.im.common.util.IdWorker;
import com.github.yuanrw.im.protobuf.generate.Internal;
import com.google.protobuf.Message;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * for client, every connection should has an ServerAckWindow
 * Date: 2019-09-08
 * Time: 20:42
 *
 * @author yrw
 */
public class ClientAckWindow {
    private static Logger logger = LoggerFactory.getLogger(ClientAckWindow.class);

    private final ChannelHandlerContext ctx;
    private final int maxSize;

    private Queue<SendMessageNode> receivedMsgQueue;
    private AtomicBoolean offer;

    public ClientAckWindow(int maxSize, ChannelHandlerContext ctx) {
        this.ctx = ctx;
        this.maxSize = maxSize;
        this.offer = new AtomicBoolean(false);
        this.receivedMsgQueue = new ConcurrentLinkedQueue<>();
    }

    /**
     * multi thread do it, it will try forever until success or the queue is full.
     *
     * @param id              msg id
     * @param from            from module
     * @param dest            dest module
     * @param receivedMsg
     * @param processFunction
     */
    public CompletableFuture<Void> offer(Long id, Internal.InternalMsg.Module from, Internal.InternalMsg.Module dest,
                                         Message receivedMsg, Consumer<Message> processFunction) {
        while (!offer.compareAndSet(false, true)) {
        }
        if (receivedMsgQueue.size() < maxSize) {
            SendMessageNode node = new SendMessageNode(id, from, dest, receivedMsg, processFunction);
            receivedMsgQueue.offer(node);
            offer.set(false);
            return processMsgAsync(node);
        } else {
            offer.set(false);
            return null;
        }
    }

    public void clean() {
        receivedMsgQueue.clear();
        offer.set(false);
    }

    private CompletableFuture<Void> processMsgAsync(SendMessageNode node) {
        return CompletableFuture.supplyAsync(node::process)
            .thenAccept(ignore -> {
                ctx.writeAndFlush(getInternalAck(node));
                receivedMsgQueue.remove(node);
            })
            .exceptionally(e -> {
                logger.error("process received msg has error", e);
                receivedMsgQueue.remove(node);
                return null;
            });
    }

    private Internal.InternalMsg getInternalAck(SendMessageNode node) {
        return Internal.InternalMsg.newBuilder()
            .setVersion(MsgVersion.V1.getVersion())
            .setId(IdWorker.genId())
            .setFrom(node.getFrom())
            .setDest(node.getDest())
            .setCreateTime(System.currentTimeMillis())
            .setMsgType(Internal.InternalMsg.MsgType.ACK)
            .setMsgBody(node.getId() + "")
            .build();
    }
}