package com.yrw.im.gateway.connector.domain;

import com.google.inject.Singleton;
import com.yrw.im.common.domain.conn.MemoryConnContext;
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

    private ConcurrentMap<Long, Serializable> userIdToNetId;

    public ClientConnContext() {
        this.connMap = new ConcurrentHashMap<>();
        this.userIdToNetId = new ConcurrentHashMap<>();
    }

    public ClientConn getConnByUserId(Long userId) {
        Serializable netId = userIdToNetId.get(userId);
        if (netId == null) {
            return null;
        }
        return connMap.get(netId);
    }

    @Override
    public void addConn(ClientConn conn) {
        Long userId = conn.getUserId();

        if (userIdToNetId.containsKey(userId)) {
            removeConn(userIdToNetId.containsKey(userId));
        }
        logger.debug("[ClientConn] user: {} is online", userId);

        connMap.putIfAbsent(conn.getNetId(), conn);
        userIdToNetId.put(conn.getUserId(), conn.getNetId());
    }
}
