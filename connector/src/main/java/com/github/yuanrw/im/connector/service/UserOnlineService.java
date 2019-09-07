package com.github.yuanrw.im.connector.service;

import com.github.yuanrw.im.common.domain.constant.MsgVersion;
import com.github.yuanrw.im.common.util.IdWorker;
import com.github.yuanrw.im.connector.domain.ClientConn;
import com.github.yuanrw.im.connector.domain.ClientConnContext;
import com.github.yuanrw.im.connector.handler.ConnectorTransferHandler;
import com.github.yuanrw.im.protobuf.generate.Ack;
import com.github.yuanrw.im.protobuf.generate.Chat;
import com.github.yuanrw.im.protobuf.generate.Internal;
import com.github.yuanrw.im.user.status.factory.UserStatusServiceFactory;
import com.github.yuanrw.im.user.status.service.UserStatusService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.protobuf.Message;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;
import java.util.Properties;

import static com.github.yuanrw.im.connector.start.ConnectorStarter.CONNECTOR_CONFIG;

/**
 * Date: 2019-05-14
 * Time: 09:53
 *
 * @author yrw
 */
@Singleton
public class UserOnlineService {

    private ClientConnContext clientConnContext;
    private ConnectorService connectorService;
    private OfflineService offlineService;
    private UserStatusService userStatusService;

    @Inject
    public UserOnlineService(OfflineService offlineService, ClientConnContext clientConnContext,
                             ConnectorService connectorService, UserStatusServiceFactory userStatusServiceFactory) {
        this.clientConnContext = clientConnContext;
        this.offlineService = offlineService;
        this.connectorService = connectorService;

        Properties properties = new Properties();
        properties.put("host", CONNECTOR_CONFIG.getRedisHost());
        properties.put("port", CONNECTOR_CONFIG.getRedisPort());
        properties.put("password", CONNECTOR_CONFIG.getRedisPassword());
        this.userStatusService = userStatusServiceFactory.createService(properties);
    }

    public void userOnline(Long msgId, String userId, ChannelHandlerContext clientConnectorCtx) {
        //user is online
        String oldConnectorId = userStatusService.online(userId, ConnectorTransferHandler.CONNECTOR_ID);
        if (oldConnectorId != null) {
            //can't online twice
            sendErrorToClient("already online", clientConnectorCtx);
        }

        //save connection
        ClientConn conn = new ClientConn(clientConnectorCtx);
        conn.setUserId(userId);
        clientConnContext.addConn(conn);

        sendAckToClient(msgId, clientConnectorCtx);

        //get all offline msg and send
        List<Message> msgs = offlineService.pollOfflineMsg(userId);
        msgs.forEach(msg -> {
            try {
                Chat.ChatMsg chatMsg = (Chat.ChatMsg) msg;
                connectorService.doChatToClientAndFlush(chatMsg);
            } catch (ClassCastException ex) {
                Ack.AckMsg ackMsg = (Ack.AckMsg) msg;
                connectorService.doSendAckToClientAndFlush(ackMsg);
            }
        });
    }

    private void sendErrorToClient(String errorMsg, ChannelHandlerContext ctx) {
        Internal.InternalMsg ack = Internal.InternalMsg.newBuilder()
            .setId(IdWorker.genId())
            .setVersion(MsgVersion.V1.getVersion())
            .setFrom(Internal.InternalMsg.Module.CONNECTOR)
            .setDest(Internal.InternalMsg.Module.CLIENT)
            .setCreateTime(System.currentTimeMillis())
            .setMsgType(Internal.InternalMsg.MsgType.ERROR)
            .setMsgBody(errorMsg)
            .build();

        ctx.writeAndFlush(ack);
    }

    private void sendAckToClient(Long id, ChannelHandlerContext ctx) {
        Internal.InternalMsg ack = Internal.InternalMsg.newBuilder()
            .setId(IdWorker.genId())
            .setVersion(MsgVersion.V1.getVersion())
            .setFrom(Internal.InternalMsg.Module.CONNECTOR)
            .setDest(Internal.InternalMsg.Module.CLIENT)
            .setCreateTime(System.currentTimeMillis())
            .setMsgType(Internal.InternalMsg.MsgType.ACK)
            .setMsgBody(id + "")
            .build();

        ctx.writeAndFlush(ack);
    }

    public void userOffline(ChannelHandlerContext ctx) {
        ClientConn conn = clientConnContext.getConn(ctx);
        if (conn == null) {
            return;
        }
        userStatusService.offline(conn.getUserId());
        //remove the connection
        clientConnContext.removeConn(ctx);
    }
}