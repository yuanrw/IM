package com.github.yuanrw.im.common.parse;

import com.github.yuanrw.im.common.function.ImBiConsumer;
import com.google.protobuf.Message;
import com.google.protobuf.ProtocolMessageEnum;
import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Date: 2019-05-23
 * Time: 18:40
 *
 * @author yrw
 */
public abstract class AbstractByEnumParser<E extends ProtocolMessageEnum, M extends Message> {

    private Map<E, ImBiConsumer<M, ChannelHandlerContext>> parseMap;

    public AbstractByEnumParser(int size) {
        this.parseMap = new HashMap<>(size);
    }

    public void register(E type, ImBiConsumer<M, ChannelHandlerContext> consumer) {
        parseMap.put(type, consumer);
    }

    /**
     * 获取枚举
     *
     * @param msg
     * @return
     */
    protected abstract E getType(M msg);

    public ImBiConsumer<M, ChannelHandlerContext> generateFun() {
        return (m, ctx) -> Optional.ofNullable(parseMap.get(getType(m)))
            .orElseThrow(() -> new IllegalArgumentException("Invalid msg enum"))
            .accept(m, ctx);
    }
}
