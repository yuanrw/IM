package com.yrw.im.transfer.domain;

import com.google.inject.Singleton;
import com.yrw.im.common.domain.conn.MemoryConnContext;
import com.yrw.im.proto.generate.Internal;
import com.yrw.im.transfer.handler.TransferStatusHandler;

import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

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

    /**
     * 缓存
     */
    private ConcurrentMap<Long, Serializable> userIdToNetId;

    public ConnectorConnContext() {
        this.userIdToNetId = new ConcurrentHashMap<>();
    }

    public ConnectorConn getConnByUserId(Long userId) throws ExecutionException, InterruptedException {
        Serializable netId = userIdToNetId.get(userId);
        if (netId == null) {
            return null;
        }
        ConnectorConn connectorConn = connMap.get(netId);
        if (connectorConn != null) {
            return connectorConn;
        }

        String netIdStr = getConnectorFromStatus(userId);
        if (netIdStr != null) {
            netId = Long.parseLong(netIdStr);
            if (connMap.containsKey(netId)) {
                userIdToNetId.put(userId, netId);
                return connMap.get(netId);
            }
        }
        return null;
    }

    private String getConnectorFromStatus(Long userId) throws ExecutionException, InterruptedException {
        final List<String> connectorId = new ArrayList<>();

        CompletableFuture<Internal.InternalMsg> future = TransferStatusHandler.createCollector(Duration.ofSeconds(10)).getFuture()
            .whenComplete((m, e) -> connectorId.add(m.getMsgBody()));

        Internal.InternalMsg req = Internal.InternalMsg.newBuilder()
            .setVersion(1)
            .setFrom(Internal.InternalMsg.Module.TRANSFER)
            .setDest(Internal.InternalMsg.Module.STATUS)
            .setCreateTime(System.currentTimeMillis())
            .setMsgType(Internal.InternalMsg.InternalMsgType.REQ)
            .setMsgBody(userId + "")
            .build();
        TransferStatusHandler.getCtx().writeAndFlush(req);

        future.get();

        return connectorId.get(0);
    }

    public void removeUser(Long userId) {
        userIdToNetId.remove(userId);
    }
}
