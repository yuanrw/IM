package com.yrw.im.status.service;

import com.yrw.im.common.exception.ImException;
import com.yrw.im.status.domain.Connector;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Date: 2019-05-19
 * Time: 21:14
 *
 * @author yrw
 */
public class UserStatusService {
    private static final String USER_CONN_STATUS_KEY = "IM:USER_CONN_STATUS:USERID:";

    private Jedis jedis;

    private ConcurrentMap<String, Connector> connectorMap;

    public UserStatusService() {
        this.jedis = new Jedis("127.0.0.1");
        this.connectorMap = new ConcurrentHashMap<>();

        Map<String, String> status = jedis.hgetAll(USER_CONN_STATUS_KEY);
        if (status != null && status.size() > 0) {
            status.keySet().forEach(k -> {
                Connector connector = connectorMap.getOrDefault(k, new Connector((k)));
                connector.addUser(Long.parseLong(status.get(k)));
            });
        }
    }

    public void online(String connectorId, Long userId) {
        Connector connector = connectorMap.get(connectorId);
        if (connector.containUser(userId) || jedis.hget(USER_CONN_STATUS_KEY, String.valueOf(userId)) != null) {
            throw new ImException("repeat.login");
        }

        connector.addUser(userId);
        //更新数据库
        jedis.hset(USER_CONN_STATUS_KEY, String.valueOf(userId), connectorId);
    }

    public void offline(String connectorId, Long userId) {
        Connector connector = connectorMap.get(connectorId);

        connector.addUser(userId);
        //更新数据库
        jedis.hdel(USER_CONN_STATUS_KEY, String.valueOf(userId));
    }

    public void connectorDone(String connectorId) {
        List<Long> users = connectorMap.get(connectorId).getUsers();
        String[] ids = users.stream().map(String::valueOf).toArray(String[]::new);
        jedis.hdel(USER_CONN_STATUS_KEY, ids);
    }

    public String getConnector(Long userId) {
        return jedis.hget(USER_CONN_STATUS_KEY, userId + "");
    }
}
