package com.github.yuanrw.im.common.domain.conn;

import io.netty.channel.ChannelHandlerContext;

import java.io.Serializable;

/**
 * Date: 2019-02-09
 * Time: 23:42
 *
 * @author yrw
 */
public class ConnectorConn extends AbstractConn {

    public ConnectorConn(ChannelHandlerContext ctx) {
        super(ctx);
    }

    @Override
    protected Serializable generateNetId(ChannelHandlerContext ctx) {
        return ctx.channel().attr(Conn.NET_ID).get();
    }
}
