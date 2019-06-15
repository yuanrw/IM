package com.yrw.im.transfer.server.domain;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.yrw.im.common.domain.conn.InternalConn;
import com.yrw.im.common.domain.conn.MemoryConnContext;
import com.yrw.im.common.util.IdWorker;
import com.yrw.im.proto.generate.Internal;
import com.yrw.im.transfer.user.status.factory.UserStatusServiceFactory;
import com.yrw.im.transfer.user.status.service.UserStatusService;
import io.netty.channel.ChannelHandlerContext;

import static com.yrw.im.transfer.server.start.TransferStarter.TRANSFER_CONFIG;

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
    public ConnectorConnContext(UserStatusServiceFactory userStatusServiceFactory) {
        this.userStatusService = userStatusServiceFactory
            .createService(TRANSFER_CONFIG.getRedisHost(), TRANSFER_CONFIG.getRedisPort());
    }

    public void online(ChannelHandlerContext ctx, Long userId) {
        String oldConnectorId = userStatusService.online(getConn(ctx).getNetId().toString(), userId);
        if (oldConnectorId != null) {
            InternalConn conn = getConn(oldConnectorId);
            if (conn != null) {
                Internal.InternalMsg forceOffline = Internal.InternalMsg.newBuilder()
                    .setVersion(1)
                    .setId(IdWorker.genId())
                    .setCreateTime(System.currentTimeMillis())
                    .setFrom(Internal.InternalMsg.Module.TRANSFER)
                    .setDest(Internal.InternalMsg.Module.CONNECTOR)
                    .setMsgType(Internal.InternalMsg.MsgType.FORCE_OFFLINE)
                    .setMsgBody(userId + "")
                    .build();

                conn.getCtx().writeAndFlush(forceOffline);
            }
        }
    }

    public void offline(Long userId) {
        userStatusService.offline(userId);
    }

    public InternalConn getConnByUserId(Long userId) {
        String connectorId = userStatusService.getConnectorId(userId);
        if (connectorId != null) {
            InternalConn conn = getConn(connectorId);
            if (conn != null) {
                return conn;
            } else {
                //connectorId已过时，而用户还没再次上线
                userStatusService.offline(userId);
            }
        }
        return null;
    }
}
