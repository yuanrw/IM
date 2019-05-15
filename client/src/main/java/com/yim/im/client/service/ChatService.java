package com.yim.im.client.service;

import com.google.inject.Inject;
import com.google.protobuf.ByteString;
import com.yim.im.client.handler.ClientHandler;
import com.yrw.im.common.domain.po.Relation;
import com.yrw.im.common.exception.ImException;
import com.yrw.im.common.util.Encryptor;
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

        byte[] content = Encryptor.encrypt(keys[0], keys[1], text.getBytes(CharsetUtil.UTF_8));

        Chat.ChatMsg chat = Chat.ChatMsg.newBuilder()
            .setId(IdWorker.genId())
            .setFromId(userId)
            .setDestId(toId)
            .setTargetType(Chat.ChatMsg.DestType.SINGLE)
            .setCreateTime(System.currentTimeMillis())
            .setMsgType(Chat.ChatMsg.MsgType.TEXT)
            .setVersion(1)
            .setToken(token)
            .setMsgBody(ByteString.copyFrom(content))
            .build();

        ClientHandler.getCtx().writeAndFlush(chat);
    }

    public void file(Long userId, Long toId, byte[] bytes) {

    }

    public void ack(Long userId, Long fromId, Long id) {
        Chat.ChatMsg ack = Chat.ChatMsg.newBuilder()
            .setVersion(1)
            .setFromId(userId)
            .setDestId(fromId)
            .setCreateTime(System.currentTimeMillis())
            .setMsgBody(ByteString.copyFromUtf8(String.valueOf(id)))
            .build();

        ClientHandler.getCtx().writeAndFlush(ack);
    }
}
