package com.yrw.im.connector.service;

import com.google.protobuf.Message;
import com.yrw.im.common.domain.conn.Conn;
import com.yrw.im.common.util.IdWorker;
import com.yrw.im.connector.domain.ClientConn;
import com.yrw.im.connector.domain.ClientConnContext;
import com.yrw.im.connector.handler.ConnectorTransferHandler;
import com.yrw.im.connector.start.ConnectorClient;
import com.yrw.im.proto.generate.Ack;
import com.yrw.im.proto.generate.Chat;

/**
 * 消息执行器
 * Date: 2019-04-08
 * Time: 21:05
 *
 * @author yrw
 */
public class ConnectorService {

    private ClientConnContext clientConnContext;

    public ConnectorService() {
        this.clientConnContext = ConnectorClient.injector.getInstance(ClientConnContext.class);
    }

    public void doChat(Chat.ChatMsg msg) {
        if (send(msg.getDestId(), msg)) {
            Ack.AckMsg delivered = Ack.AckMsg.newBuilder()
                .setId(IdWorker.genId())
                .setVersion(1)
                .setFromId(msg.getDestId())
                .setDestId(msg.getFromId())
                .setTargetType(msg.getTargetType() == Chat.ChatMsg.DestType.SINGLE ? Ack.AckMsg.DestType.SINGLE : Ack.AckMsg.DestType.GROUP)
                .setCreateTime(System.currentTimeMillis())
                .setMsgType(Ack.AckMsg.MsgType.DELIVERED)
                .setAckMsgId(msg.getId())
                .build();

            send(delivered.getDestId(), delivered);
        }
    }

    public void doSendAck(Ack.AckMsg ackMsg) {
        send(ackMsg.getDestId(), ackMsg);
    }

    private boolean send(Long destId, Message msg) {
        Conn conn = clientConnContext.getConnByUserId(destId);
        if (conn == null) {
            //不在当前机器上
            ConnectorTransferHandler.getCtx().writeAndFlush(msg);
            return false;
        } else {
            //在当前机器上，转发给用户
            //不保存历史记录
            conn.getCtx().writeAndFlush(msg);
            return true;
        }
    }

    public void forceOffline(Long userId) {
        ClientConn conn = clientConnContext.getConnByUserId(userId);
        conn.getCtx().close();
    }
}
