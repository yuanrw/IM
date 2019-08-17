package com.github.yuanrw.im.transfer.service;

import com.github.yuanrw.im.common.domain.conn.Conn;
import com.github.yuanrw.im.common.domain.conn.ConnectorConn;
import com.github.yuanrw.im.common.domain.constant.MqConstant;
import com.github.yuanrw.im.protobuf.generate.Ack;
import com.github.yuanrw.im.protobuf.generate.Chat;
import com.github.yuanrw.im.protobuf.generate.Internal;
import com.github.yuanrw.im.transfer.domain.ConnectorConnContext;
import com.github.yuanrw.im.transfer.start.TransferMqProducer;
import com.google.inject.Inject;
import com.google.protobuf.Message;
import com.rabbitmq.client.MessageProperties;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;

/**
 * Date: 2019-05-04
 * Time: 13:47
 *
 * @author yrw
 */
public class TransferService {

    private ConnectorConnContext connContext;

    @Inject
    public TransferService(ConnectorConnContext connContext) {
        this.connContext = connContext;
    }

    public void doChat(Chat.ChatMsg msg) throws IOException {
        ConnectorConn conn = connContext.getConnByUserId(msg.getDestId());

        if (conn != null) {
            conn.getCtx().writeAndFlush(msg);
        } else {
            doOffline(msg);
        }
    }

    public void doSendAck(Ack.AckMsg msg) throws IOException {
        ConnectorConn conn = connContext.getConnByUserId(msg.getDestId());

        if (conn != null) {
            conn.getCtx().writeAndFlush(msg);
        } else {
            doOffline(msg);
        }
    }

    public void doGreet(Internal.InternalMsg msg, ChannelHandlerContext ctx) {
        ctx.channel().attr(Conn.NET_ID).set(msg.getMsgBody());
        ConnectorConn conn = new ConnectorConn(ctx);
        connContext.addConn(conn);
    }

    private void doOffline(Message msg) throws IOException {
        TransferMqProducer.basicPublish(MqConstant.EXCHANGE, MqConstant.ROUTING_KEY,
            MessageProperties.PERSISTENT_TEXT_PLAIN, msg);
    }
}
