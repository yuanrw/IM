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
 * Time: 07:29
 *
 * @author yrw
 */
public class ChatApi {

    private final String connectionId;
    private UserContext userContext;
    private ClientConnectorHandler handler;

    public ChatApi(String connectionId, UserContext userContext, ClientConnectorHandler handler) {
        this.connectionId = connectionId;
        this.userContext = userContext;
        this.handler = handler;
    }

    public Chat.ChatMsg.Builder chatMsgBuilder() {
        return Chat.ChatMsg.newBuilder();
    }

    public Long text(String toId, String text) {
        checkLogin();

        Chat.ChatMsg chat = Chat.ChatMsg.newBuilder()
            .setId(IdWorker.nextId(connectionId))
            .setFromId(userContext.getUserId())
            .setDestId(toId)
            .setDestType(Chat.ChatMsg.DestType.SINGLE)
            .setCreateTime(System.currentTimeMillis())
            .setMsgType(Chat.ChatMsg.MsgType.TEXT)
            .setVersion(MsgVersion.V1.getVersion())
            .setMsgBody(ByteString.copyFrom(text, CharsetUtil.UTF_8))
            .build();

        sendToConnector(chat.getId(), chat);

        return chat.getId();
    }

    private void checkLogin() {
        if (userContext.getUserId() == null) {
            throw new ImException("client has not login!");
        }
    }

    private void sendToConnector(Long id, Message msg) {
        userContext.getClientConnectorHandler().writeAndFlush(connectionId, id, msg);
    }

    public void confirmRead(Chat.ChatMsg msg) {
        Ack.AckMsg read = Ack.AckMsg.newBuilder()
            .setId(IdWorker.nextId(connectionId))
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
