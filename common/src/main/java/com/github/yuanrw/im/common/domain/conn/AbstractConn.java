package com.github.yuanrw.im.common.domain.conn;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

import java.io.Serializable;

/**
 * Date: 2019-05-02
 * Time: 14:50
 *
 * @author yrw
 */
public abstract class AbstractConn implements Conn {

    private Serializable netId;
    private ChannelHandlerContext ctx;

    public AbstractConn(ChannelHandlerContext ctx) {
        this.ctx = ctx;
        this.netId = generateNetId(ctx);
        this.ctx.channel().attr(Conn.NET_ID).set(netId);
    }

    /**
     * 生成连接id
     *
     * @param ctx
     * @return
     */
    protected abstract Serializable generateNetId(ChannelHandlerContext ctx);

    @Override
    public Serializable getNetId() {
        return netId;
    }

    @Override
    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    @Override
    public ChannelFuture close() {
        return ctx.close();
    }
}
