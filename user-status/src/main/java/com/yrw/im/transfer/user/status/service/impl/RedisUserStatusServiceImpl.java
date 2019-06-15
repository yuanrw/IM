package com.yrw.im.transfer.user.status.service.impl;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.yrw.im.transfer.user.status.service.UserStatusService;
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
public class RedisUserStatusServiceImpl implements UserStatusService {
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
    public RedisUserStatusServiceImpl(@Assisted String host, @Assisted Integer port) {
        this.jedis = new Jedis(host, port);
        this.userIdToNetId = new ConcurrentHashMap<>(100);
    }

    @Override
    public String online(String connectorId, Long userId) {
        String oldConnectorId = jedis.hget(USER_CONN_STATUS_KEY, String.valueOf(userId));
        if (oldConnectorId != null) {
            return oldConnectorId;
        }
        userIdToNetId.put(userId, connectorId);

        jedis.hset(USER_CONN_STATUS_KEY, String.valueOf(userId), connectorId);
        return null;
    }

    @Override
    public void offline(Long userId) {
        userIdToNetId.remove(userId);

        jedis.hdel(USER_CONN_STATUS_KEY, String.valueOf(userId));
    }

    @Override
    public String getConnectorId(Long userId) {
        String connectorId = userIdToNetId.get(userId);
        if (connectorId == null) {
            connectorId = jedis.hget(USER_CONN_STATUS_KEY, userId + "");
            if (connectorId != null) {
                userIdToNetId.put(userId, connectorId);
            }
        }
        return connectorId;
    }
}
