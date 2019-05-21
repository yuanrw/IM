package com.yrw.im.status.service;

import com.google.inject.Singleton;
import com.yrw.im.common.exception.ImException;
import redis.clients.jedis.Jedis;

/**
 * 管理用户状态，没有缓存
 * Date: 2019-05-19
 * Time: 21:14
 *
 * @author yrw
 */
@Singleton
public class UserStatusService {
    private static final String USER_CONN_STATUS_KEY = "IM:USER_CONN_STATUS:USERID:";
    private static final String CONN_ONLINE_USER_SET_KEY = "IM:CONN_ONLINE_USER_SET_KEY:CONNECTOR_ID:";

    private Jedis jedis;

    /**
     * 初始化，获取用户在线数据放入内存
     */
    public UserStatusService() {
        this.jedis = new Jedis("127.0.0.1");
    }

    /**
     * 用户上线
     *
     * @param connectorId
     * @param userId
     */
    public void online(String connectorId, Long userId) {
        if (jedis.hget(USER_CONN_STATUS_KEY, String.valueOf(userId)) != null) {
            throw new ImException("repeat.login");
        }

        jedis.sadd(CONN_ONLINE_USER_SET_KEY + connectorId, userId + "");
        jedis.hset(USER_CONN_STATUS_KEY, String.valueOf(userId), connectorId);
    }

    /**
     * 用户下线
     *
     * @param connectorId
     * @param userId
     */
    public void offline(String connectorId, Long userId) {
        jedis.srem(CONN_ONLINE_USER_SET_KEY + connectorId, userId + "");
        jedis.hdel(USER_CONN_STATUS_KEY, String.valueOf(userId));
    }

    /**
     * connector宕机
     *
     * @param connectorId
     */
    public void connectorDone(String connectorId) {
        String[] userIds = (String[]) jedis.smembers(CONN_ONLINE_USER_SET_KEY + connectorId).toArray();
        jedis.hdel(USER_CONN_STATUS_KEY, userIds);
    }

    /**
     * 获取connectorId
     *
     * @param userId
     * @return
     */
    public String getConnector(Long userId) {
        return jedis.hget(USER_CONN_STATUS_KEY, userId + "");
    }
}
