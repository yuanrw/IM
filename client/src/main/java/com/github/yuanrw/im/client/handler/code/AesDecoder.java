package com.github.yuanrw.im.client.handler.code;

import com.github.yuanrw.im.client.context.UserContext;
import com.github.yuanrw.im.common.domain.po.Relation;
import com.github.yuanrw.im.common.util.Encryption;
import com.github.yuanrw.im.protobuf.generate.Chat;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

/**
 * Date: 2019-05-11
 * Time: 13:34
 *
 * @author yrw
 */
public class AesDecoder extends MessageToMessageDecoder<Message> {

    private UserContext userContext;

    public AesDecoder(UserContext userContext) {
        this.userContext = userContext;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
        if (msg instanceof Chat.ChatMsg) {
            Chat.ChatMsg cm = (Chat.ChatMsg) msg;
            Relation relation = userContext.getRelation(cm.getFromId(), cm.getDestId());
            String[] keys = relation.getEncryptKey().split("\\|");

            byte[] decodeBody = Encryption.decrypt(keys[0], keys[1], cm.getMsgBody().toByteArray());

            Chat.ChatMsg decodeMsg = Chat.ChatMsg.newBuilder().mergeFrom(cm)
                .setMsgBody(ByteString.copyFrom(decodeBody)).build();

            out.add(decodeMsg);
        } else {
            out.add(msg);
        }
    }
}
