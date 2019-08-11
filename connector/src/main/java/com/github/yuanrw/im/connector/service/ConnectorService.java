package com.github.yuanrw.im.connector.service;

import com.google.inject.Inject;
import com.google.protobuf.Message;
import com.github.yuanrw.im.common.domain.conn.Conn;
import com.github.yuanrw.im.common.util.IdWorker;
import com.github.yuanrw.im.connector.domain.ClientConnContext;
import com.github.yuanrw.im.connector.handler.ConnectorTransferHandler;
import com.github.yuanrw.im.protobuf.generate.Ack;
import com.github.yuanrw.im.protobuf.generate.Chat;

/**
 * process msg the connector received
 * Date: 2019-04-08
 * Time: 21:05
 *
 * @author yrw
 */
public class ConnectorService {

    private ClientConnContext clientConnContext;

    @Inject
    public ConnectorService(ClientConnContext clientConnContext) {
        this.clientConnContext = clientConnContext;
    }

    public void doChat(Chat.ChatMsg msg) {
        if (send(msg.getDestId(), msg)) {
            Ack.AckMsg delivered = Ack.AckMsg.newBuilder()
                .setId(IdWorker.genId())
                .setVersion(1)
                .setFromId(msg.getDestId())
                .setDestId(msg.getFromId())
                .setDestType(msg.getDestType() == Chat.ChatMsg.DestType.SINGLE ? Ack.AckMsg.DestType.SINGLE : Ack.AckMsg.DestType.GROUP)
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

    private boolean send(String destId, Message msg) {
        Conn conn = clientConnContext.getConnByUserId(destId);
        if (conn == null) {
            ConnectorTransferHandler.getCtx().writeAndFlush(msg);
            return false;
        } else {
            //the user is connected to this machine
            //won't save chat histories
            conn.getCtx().writeAndFlush(msg);
            return true;
        }
    }
}
