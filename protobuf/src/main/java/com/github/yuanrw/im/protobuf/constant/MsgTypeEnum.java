package com.github.yuanrw.im.protobuf.constant;

import com.github.yuanrw.im.protobuf.generate.Ack;
import com.github.yuanrw.im.protobuf.generate.Chat;
import com.github.yuanrw.im.protobuf.generate.Internal;

import java.util.stream.Stream;

/**
 * 消息类型
 * Date: 2019-04-14
 * Time: 15:38
 *
 * @author yrw
 */
public enum MsgTypeEnum {

    /**
     * 聊天消息
     */
    CHAT(0, Chat.ChatMsg.class),

    /**
     * 内部消息
     */
    INTERNAL(1, Internal.InternalMsg.class),

    /**
     * ack消息
     */
    ACK(2, Ack.AckMsg.class);

    int code;
    Class<?> clazz;

    MsgTypeEnum(int code, Class<?> clazz) {
        this.code = code;
        this.clazz = clazz;
    }

    public static MsgTypeEnum getByCode(int code) {
        return Stream.of(values()).filter(t -> t.code == code)
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    public static MsgTypeEnum getByClass(Class<?> clazz) {
        return Stream.of(values()).filter(t -> t.clazz == clazz)
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    public int getCode() {
        return code;
    }
}
