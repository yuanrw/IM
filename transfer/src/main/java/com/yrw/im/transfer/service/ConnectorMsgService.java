package com.yrw.im.transfer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.rabbitmq.client.MessageProperties;
import com.yrw.im.common.domain.UserStatus;
import com.yrw.im.common.domain.conn.Conn;
import com.yrw.im.common.domain.constant.MqConstant;
import com.yrw.im.common.util.IdWorker;
import com.yrw.im.proto.constant.UserStatusEnum;
import com.yrw.im.proto.generate.Chat;
import com.yrw.im.proto.generate.Internal;
import com.yrw.im.transfer.TransferMqProducer;
import com.yrw.im.transfer.domain.ConnectorConn;
import com.yrw.im.transfer.domain.ConnectorConnContext;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;

/**
 * Date: 2019-05-04
 * Time: 13:47
 *
 * @author yrw
 */
public class ConnectorMsgService {

    private ConnectorConnContext connContext;
    private ObjectMapper objectMapper;

    @Inject
    public ConnectorMsgService(ConnectorConnContext connContext) {
        this.connContext = connContext;
        this.objectMapper = new ObjectMapper();
    }

    public void doChat(Chat.ChatMsg msg) throws IOException {
        ConnectorConn conn = connContext.getConnByUserId(msg.getDestId());

        if (conn != null) {
            conn.getCtx().writeAndFlush(msg);
        } else {
            doOffline(msg);
        }
    }

    public void doGreet(ChannelHandlerContext ctx) {
        ctx.channel().attr(Conn.NET_ID).set(ctx.channel().id().asLongText());
        ConnectorConn conn = new ConnectorConn(ctx);
        connContext.addConn(conn);
    }

    public void doUpdateUserStatus(Internal.InternalMsg msg, ChannelHandlerContext ctx) throws IOException {
        UserStatus userStatus = objectMapper.readValue(msg.getMsgBody(), UserStatus.class);
        switch (UserStatusEnum.getStatus(userStatus.getStatus())) {
            case ONLINE:
                connContext.addUser(userStatus.getUserId(), ctx);
                break;
            case OFFLINE:
                connContext.removeUser(userStatus.getUserId());
                break;
            default:
        }

        sendAckToConnector(msg.getId(), ctx);
    }

    private void sendAckToConnector(Long id, ChannelHandlerContext ctx) {
        Internal.InternalMsg ack = Internal.InternalMsg.newBuilder()
            .setId(IdWorker.genId())
            .setVersion(1)
            .setFrom(Internal.InternalMsg.Module.TRANSFER)
            .setDest(Internal.InternalMsg.Module.CONNECTOR)
            .setCreateTime(System.currentTimeMillis())
            .setMsgType(Internal.InternalMsg.InternalMsgType.ACK)
            .setMsgBody(id + "")
            .build();

        ctx.writeAndFlush(ack);
    }

    public void doOffline(Chat.ChatMsg chatMsg) throws IOException {
        TransferMqProducer.getChannel().basicPublish(
            MqConstant.EXCHANGE, MqConstant.ROUTING_KEY,
            MessageProperties.PERSISTENT_TEXT_PLAIN, chatMsg.toByteArray());
    }
}
