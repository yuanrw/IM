package com.yim.im.client.api;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import com.yim.im.client.handler.ClientConnectorHandler;
import com.yrw.im.common.util.IdWorker;
import com.yrw.im.proto.generate.Ack;
import com.yrw.im.proto.generate.Chat;
import io.netty.util.CharsetUtil;

/**
 * Date: 2019-05-14
 * Time: 10:29
 *
 * @author yrw
 */
public class ChatApi {

    public Long text(Long userId, Long toId, String text, String token) {

        Chat.ChatMsg chat = Chat.ChatMsg.newBuilder()
            .setId(IdWorker.genId())
            .setFromId(userId)
            .setDestId(toId)
            .setTargetType(Chat.ChatMsg.DestType.SINGLE)
            .setCreateTime(System.currentTimeMillis())
            .setMsgType(Chat.ChatMsg.MsgType.TEXT)
            .setVersion(1)
            .setToken(token)
            .setMsgBody(ByteString.copyFrom(text, CharsetUtil.UTF_8))
            .build();

        sendToConnector(chat, chat.getId());

        return chat.getId();
    }

    public Long file(Long userId, Long toId, byte[] bytes) {
        return null;
    }

    private void sendToConnector(Message msg, Long id) {
        ClientConnectorHandler.getCtx().writeAndFlush(msg);
        ClientConnectorHandler.getClientMsgListener().hasSent(id);
    }

    public void confirmRead(Chat.ChatMsg msg) {
        Ack.AckMsg read = Ack.AckMsg.newBuilder()
            .setId(IdWorker.genId())
            .setVersion(1)
            .setFromId(msg.getDestId())
            .setDestId(msg.getFromId())
            .setCreateTime(System.currentTimeMillis())
            .setTargetType(msg.getTargetType() == Chat.ChatMsg.DestType.SINGLE ? Ack.AckMsg.DestType.SINGLE : Ack.AckMsg.DestType.GROUP)
            .setMsgType(Ack.AckMsg.MsgType.READ)
            .setAckMsgId(msg.getId())
            .build();

        ClientConnectorHandler.getCtx().writeAndFlush(read);
    }
}
