package com.github.yuanrw.im.common.domain.conn;

import io.netty.channel.ChannelHandlerContext;

import java.io.Serializable;

/**
 * 存储连接的容器
 * Date: 2019-05-04
 * Time: 11:46
 *
 * @author yrw
 */
public interface ConnContext<C extends Conn> {

    /**
     * 获取连接
     *
     * @param ctx
     * @return
     */
    C getConn(ChannelHandlerContext ctx);

    /**
     * 获取连接
     *
     * @param netId
     * @return
     */
    C getConn(Serializable netId);

    /**
     * 添加连接
     *
     * @param conn
     */
    void addConn(C conn);

    /**
     * 删除连接
     *
     * @param netId
     */
    void removeConn(Serializable netId);

    /**
     * 删除连接
     *
     * @param ctx
     */
    void removeConn(ChannelHandlerContext ctx);

    /**
     * 删除所有连接
     */
    void removeAllConn();
}
