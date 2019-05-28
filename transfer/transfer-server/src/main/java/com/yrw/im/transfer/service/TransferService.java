package com.yrw.im.transfer.service;

import com.google.inject.Inject;
import com.google.protobuf.Message;
import com.rabbitmq.client.MessageProperties;
import com.yrw.im.common.domain.conn.Conn;
import com.yrw.im.common.domain.conn.InternalConn;
import com.yrw.im.common.domain.constant.MqConstant;
import com.yrw.im.proto.constant.MsgTypeEnum;
import com.yrw.im.proto.generate.Ack;
import com.yrw.im.proto.generate.Chat;
import com.yrw.im.proto.generate.Internal;
import com.yrw.im.status.domain.ConnectorConnContext;
import com.yrw.im.transfer.start.TransferMqProducer;
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
        InternalConn conn = connContext.getConnByUserId(msg.getDestId());

        if (conn != null) {
            conn.getCtx().writeAndFlush(msg);
        } else {
            doOffline(msg);
        }
    }

    public void doSendAck(Ack.AckMsg msg) throws IOException {
        InternalConn conn = connContext.getConnByUserId(msg.getDestId());

        if (conn != null) {
            conn.getCtx().writeAndFlush(msg);
        } else {
            doOffline(msg);
        }
    }

    public void doGreet(Internal.InternalMsg msg, ChannelHandlerContext ctx) {
        ctx.channel().attr(Conn.NET_ID).set(msg.getMsgBody());
        InternalConn conn = new InternalConn(ctx);
        connContext.addConn(conn);
    }

    private void doOffline(Message msg) throws IOException {
        int code = MsgTypeEnum.getByClass(msg.getClass()).getCode();

        byte[] srcB = msg.toByteArray();
        byte[] destB = new byte[srcB.length + 1];
        destB[0] = (byte) code;

        System.arraycopy(msg.toByteArray(), 0, destB, 1, msg.toByteArray().length);

        TransferMqProducer.getChannel().basicPublish(
            MqConstant.EXCHANGE, MqConstant.ROUTING_KEY,
            MessageProperties.PERSISTENT_TEXT_PLAIN, destB);
    }
}
