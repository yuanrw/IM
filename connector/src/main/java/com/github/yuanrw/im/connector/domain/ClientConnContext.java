package com.github.yuanrw.im.connector.domain;

import com.google.inject.Singleton;
import com.github.yuanrw.im.common.domain.conn.MemoryConnContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 保存客户端连接容器
 * Date: 2019-05-02
 * Time: 14:55
 *
 * @author yrw
 */
@Singleton
public class ClientConnContext extends MemoryConnContext<ClientConn> {
    private static final Logger logger = LoggerFactory.getLogger(ClientConnContext.class);

    private ConcurrentMap<String, Serializable> userIdToNetId;

    public ClientConnContext() {
        this.connMap = new ConcurrentHashMap<>();
        this.userIdToNetId = new ConcurrentHashMap<>();
    }

    public ClientConn getConnByUserId(String userId) {
        Serializable netId = userIdToNetId.get(userId);
        if (netId == null) {
            return null;
        }
        ClientConn conn = connMap.get(netId);
        if (conn == null) {
            userIdToNetId.remove(userId);
        }
        return conn;
    }

    @Override
    public void addConn(ClientConn conn) {
        String userId = conn.getUserId();

        if (userIdToNetId.containsKey(userId)) {
            removeConn(userIdToNetId.containsKey(userId));
        }
        logger.debug("[ClientConn] user: {} is online", userId);

        connMap.putIfAbsent(conn.getNetId(), conn);
        userIdToNetId.put(conn.getUserId(), conn.getNetId());
    }
}
