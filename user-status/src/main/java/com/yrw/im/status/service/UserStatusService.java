package com.yrw.im.status.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.yrw.im.common.domain.UserStatus;
import com.yrw.im.common.domain.conn.InternalConn;
import com.yrw.im.common.domain.conn.MemoryConnContext;
import com.yrw.im.common.exception.ImException;
import com.yrw.im.proto.constant.UserStatusEnum;
import com.yrw.im.proto.generate.Internal;
import com.yrw.im.status.domain.Connector;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.yrw.im.proto.generate.Internal.InternalMsg.Module.TRANSFER;
import static com.yrw.im.status.handler.StatusServerHandler.TRANSFER_ID;

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
    private MemoryConnContext connContext;
    private ObjectMapper objectMapper;

    /**
     * 初始化，获取用户在线数据放入内存
     */
    @Inject
    public UserStatusService(MemoryConnContext<InternalConn> connContext) {
        this.jedis = new Jedis("127.0.0.1");
        this.connectorMap = new ConcurrentHashMap<>();
        this.objectMapper = new ObjectMapper();
        this.connContext = connContext;

        Map<String, String> status = jedis.hgetAll(USER_CONN_STATUS_KEY);
        if (status != null && status.size() > 0) {
            status.keySet().forEach(k -> {
                Connector connector = connectorMap.getOrDefault(k, new Connector((k)));
                connector.addUser(Long.parseLong(status.get(k)));
            });
        }
    }

    /**
     * 用户上线
     *
     * @param connectorId
     * @param userId
     */
    public void online(String connectorId, Long userId) {
        Connector connector = connectorMap.get(connectorId);
        if (connector.containUser(userId) || jedis.hget(USER_CONN_STATUS_KEY, String.valueOf(userId)) != null) {
            throw new ImException("repeat.login");
        }

        connector.addUser(userId);
        //更新数据库
        jedis.hset(USER_CONN_STATUS_KEY, String.valueOf(userId), connectorId);
    }

    /**
     * 用户下线
     *
     * @param connectorId
     * @param userId
     */
    public void offline(String connectorId, Long userId) throws JsonProcessingException {
        Connector connector = connectorMap.get(connectorId);

        connector.addUser(userId);
        //更新数据库
        jedis.hdel(USER_CONN_STATUS_KEY, String.valueOf(userId));

        UserStatus userStatus = new UserStatus();
        userStatus.setConnectorId(connectorId);
        userStatus.setUserId(userId);
        userStatus.setStatus(UserStatusEnum.OFFLINE.getCode());

        //需要通知transfer
        Internal.InternalMsg offline = Internal.InternalMsg.newBuilder()
            .setVersion(1)
            .setFrom(Internal.InternalMsg.Module.STATUS)
            .setDest(TRANSFER)
            .setCreateTime(System.currentTimeMillis())
            .setMsgType(Internal.InternalMsg.InternalMsgType.USER_STATUS)
            .setMsgBody(objectMapper.writeValueAsString(userStatus))
            .build();

        connContext.getConn(TRANSFER_ID).getCtx().writeAndFlush(offline);
    }

    /**
     * connector宕机
     *
     * @param connectorId
     */
    public void connectorDone(String connectorId) {
        List<Long> users = connectorMap.get(connectorId).getUsers();
        String[] ids = users.stream().map(String::valueOf).toArray(String[]::new);
        jedis.hdel(USER_CONN_STATUS_KEY, ids);
    }

    public String getConnector(Long userId) {
        return jedis.hget(USER_CONN_STATUS_KEY, userId + "");
    }
}
