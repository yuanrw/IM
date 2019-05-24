package com.yrw.im.status.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import redis.clients.jedis.Jedis;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 管理用户状态，有缓存
 * Date: 2019-05-19
 * Time: 21:14
 *
 * @author yrw
 */
@Singleton
public class UserStatusService {
    private static final String USER_CONN_STATUS_KEY = "IM:USER_CONN_STATUS:USERID:";

    /**
     * 缓存
     */
    private ConcurrentMap<Long, String> userIdToNetId;
    private Jedis jedis;

    /**
     * 初始化，获取用户在线数据放入内存
     */
    @Inject
    public UserStatusService() {
        this.jedis = new Jedis("127.0.0.1");
        this.userIdToNetId = new ConcurrentHashMap<>(100);
    }

    /**
     * 用户上线
     *
     * @param connectorId
     * @param userId
     * @return user本来的连接，没有则返回null
     */
    public String online(String connectorId, Long userId) {
        String oldConnectorId = jedis.hget(USER_CONN_STATUS_KEY, String.valueOf(userId));
        if (oldConnectorId != null) {
            return oldConnectorId;
        }
        userIdToNetId.put(userId, connectorId);

        jedis.hset(USER_CONN_STATUS_KEY, String.valueOf(userId), connectorId);
        return null;
    }

    /**
     * 用户下线
     *
     * @param userId
     */
    public void offline(Long userId) {
        userIdToNetId.remove(userId);

        jedis.hdel(USER_CONN_STATUS_KEY, String.valueOf(userId));
    }

    /**
     * 获取connectorId
     *
     * @param userId
     * @return
     */
    public String getConnectorId(Long userId) {
        String connectorId = userIdToNetId.get(userId);
        if (connectorId == null) {
            connectorId = jedis.hget(USER_CONN_STATUS_KEY, userId + "");
            userIdToNetId.put(userId, connectorId);
        }
        return connectorId;
    }
}
