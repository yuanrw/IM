package com.github.yuanrw.im.common.domain.ack;

import com.github.yuanrw.im.protobuf.generate.Internal;
import com.google.protobuf.Message;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Date: 2019-09-08
 * Time: 20:55
 *
 * @author yrw
 */
public class SendMessageNode implements Comparable<SendMessageNode> {

    private Long id;
    private Internal.InternalMsg.Module from;
    private Internal.InternalMsg.Module dest;

    private volatile AtomicBoolean processing;

    private Message message;

    private Consumer<Message> consumer;

    public SendMessageNode(Long id, Internal.InternalMsg.Module from, Internal.InternalMsg.Module dest,
                           Message message, Consumer<Message> consumer) {
        this.processing = new AtomicBoolean(false);
        this.id = id;
        this.from = from;
        this.dest = dest;
        this.message = message;
        this.consumer = consumer;
    }

    public Void process() {
        consumer.accept(message);
        return null;
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

    public AtomicBoolean getProcessing() {
        return processing;
    }

    public void setProcessing(AtomicBoolean processing) {
        this.processing = processing;
    }

    @Override
    public int compareTo(SendMessageNode o) {
        return this.getId().compareTo(o.getId());
    }
}
