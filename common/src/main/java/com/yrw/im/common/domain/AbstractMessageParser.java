package com.yrw.im.common.domain;

import com.google.protobuf.Message;
import com.yrw.im.common.Son;
import com.yrw.im.common.exception.ImException;
import com.yrw.im.proto.generate.Internal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Date: 2019-05-18
 * Time: 16:17
 *
 * @author yrw
 */
public class MessageParser {
    private Logger logger = LoggerFactory.getLogger(MessageParser.class);

    private Map<Class<? extends Message>, Consumer<? extends Message>> parserMap;

    public MessageParser() {
        this.parserMap = new HashMap<>();
    }

    public MessageParser checkFrom(Message message, Internal.InternalMsg.Module module) {
        if (message instanceof Internal.InternalMsg) {
            Internal.InternalMsg m = (Internal.InternalMsg) message;
            if (m.getFrom() != module) {
                throw new ImException("from unknown");
            }
        }
        return this;
    }

    public MessageParser checkDest(Message message, Internal.InternalMsg.Module module) {
        if (message instanceof Internal.InternalMsg) {
            Internal.InternalMsg m = (Internal.InternalMsg) message;
            if (m.getDest() != module) {
                throw new ImException("dest not me");
            }
        }
        return this;
    }

    public <T extends Message> void register(Class<T> clazz, Consumer<T> consumer) {
        parserMap.put(clazz, consumer);
    }

    @SuppressWarnings("unchecked")
    public MessageParser parse(Message message) {
        Consumer consumer = parserMap.get(message.getClass());
        consumer.accept(message.getClass().cast(message));
        return this;
    }

    public static void main(String[] args) {
        Son so = new Son();
        MessageParser.test(so);
    }

    public static void test(Fa fa) {
        System.out.println(fa.getClass());
        test1(fa.getClass().cast(fa));
    }

    public static void test1(Son son) {
        System.out.println(son);
    }
}
