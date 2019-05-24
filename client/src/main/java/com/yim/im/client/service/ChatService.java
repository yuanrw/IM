package com.yim.im.client.service;

import com.google.inject.Inject;
import com.google.protobuf.ByteString;
import com.yim.im.client.handler.ClientConnectorHandler;
import com.yrw.im.common.domain.po.Relation;
import com.yrw.im.common.exception.ImException;
import com.yrw.im.common.util.IdWorker;
import com.yrw.im.proto.generate.Chat;
import io.netty.util.CharsetUtil;

/**
 * Date: 2019-04-15
 * Time: 17:53
 *
 * @author yrw
 */
public class ChatService {

    private ClientRestService clientRestService;

    @Inject
    public ChatService(ClientRestService clientRestService) {
        this.clientRestService = clientRestService;
    }

    public void text(Long userId, Long toId, String text, String token) {

        Relation relation = clientRestService.relation(userId, toId, token);
        if (relation == null) {
            throw new ImException("friend.not.found");
        }
        String[] keys = relation.getEncryptKey().split("\\|");

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

        ClientConnectorHandler.getCtx().writeAndFlush(chat);
    }

    public void file(Long userId, Long toId, byte[] bytes) {

    }

    public void ack(Long userId, Long fromId, Long id) {
        Chat.ChatMsg ack = Chat.ChatMsg.newBuilder()
            .setId(IdWorker.genId())
            .setVersion(1)
            .setFromId(userId)
            .setDestId(fromId)
            .setCreateTime(System.currentTimeMillis())
            .setMsgBody(ByteString.copyFromUtf8(String.valueOf(id)))
            .build();

        ClientConnectorHandler.getCtx().writeAndFlush(ack);
    }
}
