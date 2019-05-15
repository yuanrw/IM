package com.yrw.im.gateway.connector.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.yrw.im.common.domain.UserStatus;
import com.yrw.im.gateway.connector.domain.ClientConn;
import com.yrw.im.gateway.connector.domain.ClientConnContext;
import com.yrw.im.gateway.connector.handler.ConnectorTransferHandler;
import com.yrw.im.gateway.connector.start.ConnectorClient;
import com.yrw.im.proto.constant.UserStatusEnum;
import com.yrw.im.proto.generate.Internal;
import io.netty.channel.ChannelHandlerContext;

/**
 * Date: 2019-05-14
 * Time: 09:53
 *
 * @author yrw
 */
public class UserStatusService {

    private ClientConnContext clientConnContext;
    private ObjectMapper objectMapper;

    @Inject
    public UserStatusService() {
        this.clientConnContext = ConnectorClient.injector.getInstance(ClientConnContext.class);
        this.objectMapper = new ObjectMapper();
    }

    public void userOnline(Internal.InternalMsg msg, ChannelHandlerContext ctx) throws JsonProcessingException {
        //保存连接
        ClientConn conn = new ClientConn(ctx);
        Long userId = Long.parseLong(msg.getMsgBody());
        conn.setUserId(userId);

        clientConnContext.addConn(conn);

        //向transfer同步用户状态
        UserStatus userStatus = new UserStatus();
        userStatus.setUserId(userId);
        userStatus.setStatus(UserStatusEnum.ONLINE.getCode());

        Internal.InternalMsg statusMsg = Internal.InternalMsg.newBuilder()
            .setVersion(1)
            .setFrom(Internal.InternalMsg.Module.CONNECTOR)
            .setDest(Internal.InternalMsg.Module.TRANSFER)
            .setCreateTime(System.currentTimeMillis())
            .setMsgType(Internal.InternalMsg.InternalMsgType.USER_STATUS)
            .setMsgBody(objectMapper.writeValueAsString(userStatus))
            .build();
        ConnectorTransferHandler.getCtx().writeAndFlush(statusMsg);
    }

    public void userOffline(ChannelHandlerContext ctx) throws JsonProcessingException {
        ClientConn conn = clientConnContext.getConn(ctx);

        //移除连接
        clientConnContext.removeConn(ctx);

        //向transfer同步用户状态
        UserStatus userStatus = new UserStatus();
        userStatus.setUserId(conn.getUserId());
        userStatus.setStatus(UserStatusEnum.OFFLINE.getCode());

        Internal.InternalMsg statusMsg = Internal.InternalMsg.newBuilder()
            .setVersion(1)
            .setFrom(Internal.InternalMsg.Module.CONNECTOR)
            .setDest(Internal.InternalMsg.Module.TRANSFER)
            .setCreateTime(System.currentTimeMillis())
            .setMsgType(Internal.InternalMsg.InternalMsgType.USER_STATUS)
            .setMsgBody(objectMapper.writeValueAsString(userStatus))
            .build();
        ConnectorTransferHandler.getCtx().writeAndFlush(statusMsg);
    }
}
