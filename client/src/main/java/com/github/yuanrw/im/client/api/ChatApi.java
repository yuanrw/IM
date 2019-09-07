package com.github.yuanrw.im.client.api;

import com.github.yuanrw.im.client.context.UserContext;
import com.github.yuanrw.im.client.handler.ClientConnectorHandler;
import com.github.yuanrw.im.common.domain.constant.MsgVersion;
import com.github.yuanrw.im.common.exception.ImException;
import com.github.yuanrw.im.common.util.IdWorker;
import com.github.yuanrw.im.protobuf.generate.Ack;
import com.github.yuanrw.im.protobuf.generate.Chat;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import io.netty.util.CharsetUtil;

/**
 * Date: 2019-05-14
 * Time: 10:29
 *
 * @author yrw
 */
public class ChatApi {

    private UserContext userContext;
    private ClientConnectorHandler handler;

    public ChatApi(UserContext userContext, ClientConnectorHandler handler) {
        this.userContext = userContext;
        this.handler = handler;
    }

    public Chat.ChatMsg.Builder chatMsgBuilder() {
        return Chat.ChatMsg.newBuilder();
    }

    public Long send(Chat.ChatMsg chat) {
        checkLogin();

        sendToConnector(chat, chat.getId());

        return chat.getId();
    }

    public Long text(String toId, String text) {
        checkLogin();

        Chat.ChatMsg chat = Chat.ChatMsg.newBuilder()
            .setId(IdWorker.genId())
            .setFromId(userContext.getUserId())
            .setDestId(toId)
            .setDestType(Chat.ChatMsg.DestType.SINGLE)
            .setCreateTime(System.currentTimeMillis())
            .setMsgType(Chat.ChatMsg.MsgType.TEXT)
            .setVersion(MsgVersion.V1.getVersion())
            .setMsgBody(ByteString.copyFrom(text, CharsetUtil.UTF_8))
            .build();

        sendToConnector(chat, chat.getId());

        return chat.getId();
    }

    private void checkLogin() {
        if (userContext.getUserId() == null) {
            throw new ImException("client has not login!");
        }
    }

    private void sendToConnector(Message msg, Long id) {
        userContext.getClientConnectorHandler().writeAndFlush(msg, id);
    }

    public void confirmRead(Chat.ChatMsg msg) {
        Ack.AckMsg read = Ack.AckMsg.newBuilder()
            .setId(IdWorker.genId())
            .setVersion(MsgVersion.V1.getVersion())
            .setFromId(msg.getDestId())
            .setDestId(msg.getFromId())
            .setCreateTime(System.currentTimeMillis())
            .setDestType(msg.getDestType() == Chat.ChatMsg.DestType.SINGLE ? Ack.AckMsg.DestType.SINGLE : Ack.AckMsg.DestType.GROUP)
            .setMsgType(Ack.AckMsg.MsgType.READ)
            .setAckMsgId(msg.getId())
            .build();

        handler.getCtx().writeAndFlush(read);
    }
}
