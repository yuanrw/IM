package com.yrw.im.common.domain;

import com.google.protobuf.Message;
import com.yrw.im.common.exception.ImException;
import com.yrw.im.proto.generate.Internal;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Date: 2019-05-18
 * Time: 16:17
 *
 * @author yrw
 */
public abstract class AbstractMessageParser {
    private Logger logger = LoggerFactory.getLogger(AbstractMessageParser.class);

    private Map<Class<? extends Message>, BiConsumer<? extends Message, ChannelHandlerContext>> parserMap;

    protected AbstractMessageParser() {
        this.parserMap = new HashMap<>();
        registerParsers();
    }

    public static void checkFrom(Message message, Internal.InternalMsg.Module module) {
        if (message instanceof Internal.InternalMsg) {
            Internal.InternalMsg m = (Internal.InternalMsg) message;
            if (m.getFrom() != module) {
                throw new ImException("from unknown");
            }
        }
    }

    public static void checkDest(Message message, Internal.InternalMsg.Module module) {
        if (message instanceof Internal.InternalMsg) {
            Internal.InternalMsg m = (Internal.InternalMsg) message;
            if (m.getDest() != module) {
                throw new ImException("dest not me");
            }
        }
    }

    /**
     * 注册msg处理方法
     */
    public abstract void registerParsers();

    protected <T extends Message> void register(Class<T> clazz, BiConsumer<T, ChannelHandlerContext> consumer) {
        parserMap.put(clazz, consumer);
    }

    @SuppressWarnings("unchecked")
    public AbstractMessageParser parse(Message msg, ChannelHandlerContext ctx) {
        BiConsumer consumer = parserMap.get(msg.getClass());
        if (consumer == null) {
            logger.warn("[message parser] unexpected msg: {}", msg.toString());
            return this;
        }
        doParse(consumer, msg.getClass(), msg, ctx);
        return this;
    }

    private <T extends Message> void doParse(BiConsumer<T, ChannelHandlerContext> consumer, Class<T> clazz, Message msg, ChannelHandlerContext ctx) {
        T m = clazz.cast(msg);
        consumer.accept(m, ctx);
    }
}
