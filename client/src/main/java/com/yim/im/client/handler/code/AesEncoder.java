package com.yim.im.client.handler.code;

import com.google.inject.Inject;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import com.yim.im.client.context.UserContext;
import com.yrw.im.common.code.MsgDecoder;
import com.yrw.im.common.domain.po.Relation;
import com.yrw.im.common.util.Encryption;
import com.yrw.im.proto.generate.Chat;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Date: 2019-05-11
 * Time: 12:49
 *
 * @author yrw
 */
public class AesEncoder extends MessageToMessageEncoder<Message> {
    private static final Logger logger = LoggerFactory.getLogger(MsgDecoder.class);

    private UserContext userContext;

    @Inject
    public AesEncoder(UserContext userContext) {
        this.userContext = userContext;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
        try {
            if (msg instanceof Chat.ChatMsg) {
                Chat.ChatMsg cm = (Chat.ChatMsg) msg;
                Relation relation = userContext.getRelation(cm.getFromId(), cm.getDestId());
                String[] keys = relation.getEncryptKey().split("\\|");

                byte[] encodeBody = Encryption.encrypt(keys[0], keys[1], cm.getMsgBody().toByteArray());

                Chat.ChatMsg encodeMsg = Chat.ChatMsg.newBuilder().mergeFrom(cm)
                    .setMsgBody(ByteString.copyFrom(encodeBody)).build();

                logger.debug("[encode] encode message");

                out.add(encodeMsg);
            } else {
                out.add(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
