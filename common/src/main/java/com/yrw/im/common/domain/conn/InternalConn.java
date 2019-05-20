package com.yrw.im.common.domain.conn;

import io.netty.channel.ChannelHandlerContext;

import java.io.Serializable;

/**
 * Date: 2019-02-09
 * Time: 23:42
 *
 * @author yrw
 */
public class InternalConn extends AbstractConn {

    public InternalConn(ChannelHandlerContext ctx) {
        super(ctx);
    }

    @Override
    protected Serializable generateNetId(ChannelHandlerContext ctx) {
        return ctx.channel().attr(InternalConn.NET_ID).get();
    }
}
