package com.github.yuanrw.im.connector.domain;

import com.github.yuanrw.im.common.domain.conn.AbstractConn;
import io.netty.channel.ChannelHandlerContext;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Date: 2019-05-04
 * Time: 11:54
 *
 * @author yrw
 */
public class ClientConn extends AbstractConn {

    private static final AtomicLong NETID_GENERATOR = new AtomicLong(0);

    private String userId;

    public ClientConn(ChannelHandlerContext ctx) {
        super(ctx);
    }

    @Override
    protected Serializable generateNetId(ChannelHandlerContext ctx) {
        return NETID_GENERATOR.getAndIncrement();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
