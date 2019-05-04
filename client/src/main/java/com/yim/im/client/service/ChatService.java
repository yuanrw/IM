package com.yim.im.client.service;

import com.google.protobuf.ByteString;
import com.yrw.im.proto.generate.Chat;
import com.yrw.im.proto.generate.Internal;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.CharsetUtil;

/**
 * Date: 2019-04-15
 * Time: 17:53
 *
 * @author yrw
 */
public class ChatService {

    private ChannelHandlerContext ctx;

    public void greet(Long userId, ChannelHandlerContext ctx) {

        Internal.InternalMsg greet = Internal.InternalMsg.newBuilder()
            .setFrom(Internal.InternalMsg.Module.CLIENT)
            .setDest(Internal.InternalMsg.Module.CONNECTOR)
            .setCreateTime(System.currentTimeMillis())
            .setVersion(1)
            .setMsgType(Internal.InternalMsg.InternalMsgType.GREET)
            .setMsgBody(String.valueOf(userId))
            .build();

        ctx.writeAndFlush(greet);
    }

    public void text(Long userId, Long toId, String text, String token) {

        Chat.ChatMsg chat = Chat.ChatMsg.newBuilder()
            .setFromId(userId)
            .setDestId(toId)
            .setTargetType(Chat.ChatMsg.DestType.SINGLE)
            .setCreateTime(System.currentTimeMillis())
            .setMsgType(Chat.ChatMsg.MsgType.TEXT)
            .setVersion(1)
            .setToken(token)
            .setMsgBody(ByteString.copyFrom(text, CharsetUtil.UTF_8))
            .build();

        ctx.writeAndFlush(chat);
    }

    public void file(Long userId, Long toId, byte[] bytes) {

    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }
}
