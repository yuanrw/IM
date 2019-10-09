package com.github.yuanrw.im.common.domain.ack;

import com.github.yuanrw.im.common.domain.constant.MsgVersion;
import com.github.yuanrw.im.common.util.IdWorker;
import com.github.yuanrw.im.protobuf.generate.Internal;
import com.google.protobuf.Message;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * msg need to be processed from server
 * Date: 2019-09-08
 * Time: 20:55
 *
 * @author yrw
 */
public class ProcessMsgNode {

    private Long id;
    private Internal.InternalMsg.Module from;
    private Internal.InternalMsg.Module dest;
    private ChannelHandlerContext ctx;

    private CompletableFuture<Void> future;

    private Message message;

    private Consumer<Message> consumer;

    public ProcessMsgNode(Long id, Internal.InternalMsg.Module from, Internal.InternalMsg.Module dest,
                          ChannelHandlerContext ctx, Message message, Consumer<Message> consumer) {
        this.id = id;
        this.from = from;
        this.dest = dest;
        this.ctx = ctx;
        this.message = message;
        this.consumer = consumer;
        this.future = new CompletableFuture<>();
    }

    public Void process() {
        consumer.accept(message);
        return null;
    }

    public void sendAck() {
        if (ctx.channel().isOpen()) {
            Internal.InternalMsg ack = Internal.InternalMsg.newBuilder()
                .setVersion(MsgVersion.V1.getVersion())
                .setId(IdWorker.snowGenId())
                .setFrom(from)
                .setDest(dest)
                .setCreateTime(System.currentTimeMillis())
                .setMsgType(Internal.InternalMsg.MsgType.ACK)
                .setMsgBody(id + "")
                .build();

            ctx.writeAndFlush(ack);
        }
    }

    public void complete() {
        this.future.complete(null);
    }

    public CompletableFuture<Void> getFuture() {
        return future;
    }

    public Long getId() {
        return id;
    }

    public Internal.InternalMsg.Module getFrom() {
        return from;
    }

    public Internal.InternalMsg.Module getDest() {
        return dest;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }
}
