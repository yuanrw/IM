package com.yrw.im.gateway.connector.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.yrw.im.common.domain.UserStatus;
import com.yrw.im.common.domain.conn.Conn;
import com.yrw.im.gateway.connector.domain.ClientConn;
import com.yrw.im.gateway.connector.domain.ClientConnContext;
import com.yrw.im.gateway.connector.handler.ConnectorTransferHandler;
import com.yrw.im.proto.constant.UserStatusEnum;
import com.yrw.im.proto.generate.Chat;
import com.yrw.im.proto.generate.Internal;
import io.netty.channel.ChannelHandlerContext;

/**
 * 消息执行器
 * Date: 2019-04-08
 * Time: 21:05
 *
 * @author yrw
 */
public class ClientMsgService {

    private ClientConnContext clientConnContext;
    private ObjectMapper objectMapper;

    @Inject
    public ClientMsgService(ClientConnContext clientConnContext) {
        this.clientConnContext = clientConnContext;
        this.objectMapper = new ObjectMapper();
    }

    public void doGreet(Internal.InternalMsg msg, ChannelHandlerContext ctx) throws JsonProcessingException {
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

    public void doChat(Chat.ChatMsg msg) {
        Conn conn = clientConnContext.getConnByUserId(msg.getDestId());
        if (conn == null) {
            //不在当前机器上
            ConnectorTransferHandler.getCtx().writeAndFlush(msg);
        } else {
            //在当前机器上，转发给用户
            //不保存历史记录
            conn.getCtx().writeAndFlush(msg);
        }
    }
}
