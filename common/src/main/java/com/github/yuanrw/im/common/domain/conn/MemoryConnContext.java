package com.github.yuanrw.im.common.domain.conn;

import com.google.inject.Singleton;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 使用内存存储连接
 * Date: 2019-05-04
 * Time: 12:52
 *
 * @author yrw
 */
@Singleton
public class MemoryConnContext<C extends Conn> implements ConnContext<C> {
    private static final Logger logger = LoggerFactory.getLogger(MemoryConnContext.class);

    protected ConcurrentMap<Serializable, C> connMap;

    public MemoryConnContext() {
        this.connMap = new ConcurrentHashMap<>();
    }

    @Override
    public C getConn(ChannelHandlerContext ctx) {
        Serializable netId = ctx.channel().attr(Conn.NET_ID).get();
        if (netId == null) {
            logger.warn("Conn netId not found in ctx, ctx: {}", ctx.toString());
            return null;
        }

        C conn = connMap.get(netId);
        if (conn == null) {
            logger.warn("Conn not found, netId: {}", netId);
        }
        return conn;
    }

    @Override
    public C getConn(Serializable netId) {
        C conn = connMap.get(netId);
        if (conn == null) {
            logger.warn("Conn not found, netId: {}", netId);
        }
        return conn;
    }

    @Override
    public void addConn(C conn) {
        logger.debug("add a conn, netId: {}", conn.getNetId());
        connMap.put(conn.getNetId(), conn);
    }

    @Override
    public void removeConn(Serializable netId) {
        connMap.computeIfPresent(netId, (id, c) -> {
            c.close();
            return null;
        });
    }

    @Override
    public void removeConn(ChannelHandlerContext ctx) {
        Serializable netId = ctx.channel().attr(Conn.NET_ID).get();
        if (netId == null) {
            logger.warn("Can't find a netId for the ctx");
        } else {
            removeConn(netId);
        }
    }

    @Override
    public void removeAllConn() {
        connMap.clear();
    }
}
