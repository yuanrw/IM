package com.yrw.im.transfer.domain;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.yrw.im.common.domain.conn.InternalConn;
import com.yrw.im.common.domain.conn.MemoryConnContext;
import com.yrw.im.status.service.UserStatusService;
import io.netty.channel.ChannelHandlerContext;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 存储transfer和connector的连接
 * 以及用户和connector的关系
 * Date: 2019-04-12
 * Time: 18:22
 *
 * @author yrw
 */
@Singleton
public class ConnectorConnContext extends MemoryConnContext<InternalConn> {

    /**
     * user与connector连接关系的缓存
     */
    private ConcurrentMap<Long, Serializable> userIdToNetId;
    private UserStatusService userStatusService;

    @Inject
    public ConnectorConnContext(UserStatusService userStatusService) {
        this.userIdToNetId = new ConcurrentHashMap<>();
        this.userStatusService = userStatusService;
    }

    public void online(ChannelHandlerContext ctx, Long userId) {
        userStatusService.online(getConn(ctx).getNetId().toString(), userId);
    }

    public void offline(ChannelHandlerContext ctx, Long userId) {
        userStatusService.offline(getConn(ctx).getNetId().toString(), userId);
    }

    public InternalConn getConnByUserId(Long userId) {
        Serializable connectorId = userIdToNetId.get(userId);
        if (connectorId != null) {
            InternalConn conn = getConn(connectorId);
            if (conn != null) {
                return conn;
            } else {
                //connectorId已过时（connector下线过）
                userIdToNetId.remove(userId);
            }
        }

        //从db中获取user连接的connectorId
        connectorId = userStatusService.getConnector(userId);
        if (connectorId != null) {
            if (connMap.containsKey(connectorId)) {
                userIdToNetId.put(userId, connectorId);
                return getConn(connectorId);
            }
        }
        return null;
    }

    @Override
    public void removeConn(ChannelHandlerContext ctx) {
        super.removeConn(ctx);

        userStatusService.connectorDone(getConn(ctx).getNetId().toString());
    }
}
