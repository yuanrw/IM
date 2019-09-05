package com.github.yuanrw.im.user.status.service.impl;

import com.github.yuanrw.im.user.status.service.UserStatusService;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Properties;

/**
 * manage user status in redis
 * Date: 2019-05-19
 * Time: 21:14
 *
 * @author yrw
 */
public class RedisUserStatusServiceImpl implements UserStatusService {
    private static final Logger logger = LoggerFactory.getLogger(RedisUserStatusServiceImpl.class);
    private static final String USER_CONN_STATUS_KEY = "IM:USER_CONN_STATUS:USERID:";

    private JedisPool jedisPool;

    @Inject
    public RedisUserStatusServiceImpl(@Assisted Properties properties) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxWaitMillis(2 * 1000);
        String password = properties.getProperty("password");
        jedisPool = new JedisPool(config, properties.getProperty("host"), (Integer) properties.get("port"),
            2 * 1000, password != null && !password.isEmpty() ? password : null);
    }

    @Override
    public String online(String userId, String connectorId) {
        logger.debug("[user status] user online: userId: {}, connectorId: {}", userId, connectorId);

        try (Jedis jedis = jedisPool.getResource()) {
            String oldConnectorId = jedis.hget(USER_CONN_STATUS_KEY, String.valueOf(userId));
            jedis.hset(USER_CONN_STATUS_KEY, String.valueOf(userId), connectorId);
            return oldConnectorId;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void offline(String userId) {
        logger.debug("[user status] user offline: userId: {}", userId);

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hdel(USER_CONN_STATUS_KEY, String.valueOf(userId));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public String getConnectorId(String userId) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hget(USER_CONN_STATUS_KEY, userId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }
}