package com.yrw.im.common.domain;

import com.yrw.im.common.function.ImBiConsumer;
import com.yrw.im.proto.generate.Internal;
import io.netty.channel.ChannelHandlerContext;

import java.util.Map;
import java.util.Optional;

/**
 * Date: 2019-05-23
 * Time: 18:40
 *
 * @author yrw
 */
public class InternalMsgParserGenerator {

    private Map<Internal.InternalMsg.InternalMsgType, ImBiConsumer<Internal.InternalMsg, ChannelHandlerContext>> parseMap;

    public void put(Internal.InternalMsg.InternalMsgType type, ImBiConsumer<Internal.InternalMsg, ChannelHandlerContext> consumer) {
        parseMap.put(type, consumer);
    }

    public ImBiConsumer<Internal.InternalMsg, ChannelHandlerContext> generate() {
        ImBiConsumer<Internal.InternalMsg, ChannelHandlerContext> function = (m, ctx) -> {
            Optional.ofNullable(parseMap.get(m))
                .orElseThrow(() -> new IllegalArgumentException("Invalid internal msg type"));
        }
    }

}
