package com.yrw.im.transfer.domain;

import com.google.inject.Singleton;
import com.yrw.im.common.domain.conn.MemoryConnContext;
import com.yrw.im.common.exception.ImException;
import io.netty.channel.ChannelHandlerContext;
import redis.clients.jedis.Jedis;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 内存存储transfer和connector的连接
 * redis存储userId和netId的关系
 * Date: 2019-04-12
 * Time: 18:22
 *
 * @author yrw
 */
@Singleton
public class ConnectorConnContext extends MemoryConnContext<ConnectorConn> {

    private static final String USER_CONN_STATUS_KEY = "IM:USER_CONN_STATUS:USERID:";
    private Jedis jedis;
    /**
     * 缓存
     */
    private ConcurrentMap<Long, Serializable> userIdToNetId;

    public ConnectorConnContext() {
        this.jedis = new Jedis("127.0.0.1");
        this.userIdToNetId = new ConcurrentHashMap<>();
    }

    public ConnectorConn getConnByUserId(Long userId) {
        Serializable netId = userIdToNetId.get(userId);
        if (netId == null) {
            return null;
        }
        ConnectorConn connectorConn = connMap.get(netId);
        if (connectorConn != null) {
            return connectorConn;
        }

        String netIdStr = jedis.hget(USER_CONN_STATUS_KEY, String.valueOf(userId));
        if (netIdStr != null) {
            netId = Long.parseLong(netIdStr);
            if (connMap.containsKey(netId)) {
                userIdToNetId.put(userId, netId);
                return connMap.get(netId);
            }
        }
        return null;
    }

    public void addUser(Long userId, ChannelHandlerContext ctx) {
        boolean online = userIdToNetId.containsKey(userId) || jedis.hget(USER_CONN_STATUS_KEY, String.valueOf(userId)) != null;
        if (online) {
            throw new ImException("repeat.login");
        }
        Serializable netId = getConn(ctx).getNetId();
        userIdToNetId.put(userId, netId);
        //更新数据库
        jedis.hset(USER_CONN_STATUS_KEY, String.valueOf(userId), (String) netId);
    }

    public void removeUser(Long userId, ChannelHandlerContext ctx) {
        userIdToNetId.remove(userId);
        //更新数据库
        jedis.hdel(USER_CONN_STATUS_KEY, String.valueOf(userId));
    }

    /**
     * connector与transfer断开连接后，需要清除connector信息
     *
     * @param netId
     */
    public void removeConnector(Long netId) {
        removeConn(netId);

        for (Map.Entry<Long, Serializable> entry : userIdToNetId.entrySet()) {
            if (entry.getValue().equals(netId)) {
                userIdToNetId.remove(entry.getKey());
            }
        }
    }
}
