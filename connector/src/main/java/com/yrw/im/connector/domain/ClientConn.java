package com.yrw.im.connector.domain;

import com.yrw.im.common.domain.conn.AbstractConn;
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

    private Long userId;

    public ClientConn(ChannelHandlerContext ctx) {
        super(ctx);
    }

    @Override
    protected Serializable generateNetId(ChannelHandlerContext ctx) {
        return NETID_GENERATOR.getAndIncrement();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
