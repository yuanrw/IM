package com.github.yuanrw.im.common.parse;

import com.github.yuanrw.im.common.exception.ImException;
import com.github.yuanrw.im.protobuf.constant.MsgTypeEnum;
import com.github.yuanrw.im.protobuf.generate.Ack;
import com.github.yuanrw.im.protobuf.generate.Chat;
import com.github.yuanrw.im.protobuf.generate.Internal;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

import java.util.HashMap;
import java.util.Map;

/**
 * Date: 2019-04-14
 * Time: 16:09
 *
 * @author yrw
 */
public class ParseService {

    private Map<MsgTypeEnum, Parse> parseFunctionMap;

    public ParseService() {
        parseFunctionMap = new HashMap<>(MsgTypeEnum.values().length);

        parseFunctionMap.put(MsgTypeEnum.CHAT, Chat.ChatMsg::parseFrom);
        parseFunctionMap.put(MsgTypeEnum.INTERNAL, Internal.InternalMsg::parseFrom);
        parseFunctionMap.put(MsgTypeEnum.ACK, Ack.AckMsg::parseFrom);
    }

    public Message getMsgByCode(int code, byte[] bytes) throws InvalidProtocolBufferException {
        MsgTypeEnum msgType = MsgTypeEnum.getByCode(code);
        Parse parseFunction = parseFunctionMap.get(msgType);
        if (parseFunction == null) {
            throw new ImException("[msg parse], no proper parse function, msgType: " + msgType.name());
        }
        return parseFunction.process(bytes);
    }

    @FunctionalInterface
    public interface Parse {
        /**
         * parse msg
         *
         * @param bytes msg bytes
         * @return
         * @throws InvalidProtocolBufferException
         */
        Message process(byte[] bytes) throws InvalidProtocolBufferException;
    }
}
