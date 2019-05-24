package com.yrw.im.transfer.domain;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.yrw.im.common.domain.conn.InternalConn;
import com.yrw.im.common.domain.conn.MemoryConnContext;
import com.yrw.im.status.service.UserStatusService;
import io.netty.channel.ChannelHandlerContext;

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

    private UserStatusService userStatusService;

    @Inject
    public ConnectorConnContext(UserStatusService userStatusService) {
        this.userStatusService = userStatusService;
    }

    public void online(ChannelHandlerContext ctx, Long userId) {
        userStatusService.online(getConn(ctx).getNetId().toString(), userId);
    }

    public void offline(ChannelHandlerContext ctx, Long userId) {
        userStatusService.offline(getConn(ctx).getNetId().toString(), userId);
    }

    public InternalConn getConnByUserId(Long userId) {
        String connectorId = userStatusService.getConnectorId(userId);
        if (connectorId != null) {
            InternalConn conn = getConn(connectorId);
            if (conn != null) {
                return conn;
            } else {
                //connector宕机过
                userStatusService.connectorDone(connectorId);
            }
        }
        return null;
    }

    @Override
    public void removeConn(ChannelHandlerContext ctx) {
        userStatusService.connectorDone(getConn(ctx).getNetId().toString());
        super.removeConn(ctx);
    }
}
