package com.github.yuanrw.im.common.test

import com.github.yuanrw.im.common.domain.ResponseCollector
import com.github.yuanrw.im.common.domain.constant.MsgVersion
import com.github.yuanrw.im.protobuf.generate.Chat
import com.google.protobuf.ByteString
import io.netty.util.CharsetUtil
import spock.lang.Specification

/**
 * Date: 2019-05-31
 * Time: 18:49
 * @author yrw
 */
class ResponseCollectorTest extends Specification {

    def "test retry"() {
        given:
        def chat = Chat.ChatMsg.newBuilder()
                .setId(1)
                .setFromId("123")
                .setDestId("456")
                .setDestType(Chat.ChatMsg.DestType.SINGLE)
                .setCreateTime(System.currentTimeMillis())
                .setMsgType(Chat.ChatMsg.MsgType.TEXT)
                .setVersion(MsgVersion.V1.getVersion())
                .setMsgBody(ByteString.copyFrom("hello", CharsetUtil.UTF_8))
                .build()

        def send = new ArrayList()

        def msgResponseCollector = new ResponseCollector(chat, { m -> send.add(m) })

        when:
        msgResponseCollector.send()

        then:
        send.size() == 1
        !msgResponseCollector.getSending().get()
    }
}
