package com.yrw.im.common.domain.conn;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

import java.io.Serializable;

/**
 * 连接
 * Date: 2019-05-02
 * Time: 14:50
 *
 * @author yrw
 */
public interface Conn {

    AttributeKey<Serializable> NET_ID = AttributeKey.valueOf("netId");

    /**
     * 获取连接id
     *
     * @return
     */
    Serializable getNetId();

    /**
     * 获取ChannelHandlerContext
     *
     * @return
     */
    ChannelHandlerContext getCtx();
}
