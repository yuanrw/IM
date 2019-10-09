package com.github.yuanrw.im.common.test

import com.github.yuanrw.im.common.domain.ack.ServerAckWindow
import com.github.yuanrw.im.common.domain.constant.MsgVersion
import com.github.yuanrw.im.protobuf.generate.Chat
import com.github.yuanrw.im.protobuf.generate.Internal
import com.google.protobuf.ByteString
import io.netty.util.CharsetUtil
import spock.lang.Specification

import java.time.Duration

/**
 * Date: 2019-09-08
 * Time: 15:53
 * @author yrw
 */
class ServerAckWindowTest extends Specification {

    void testGetAckInTime() {
        given:
        def serverAckWindow = new ServerAckWindow("1", 5, Duration.ofMillis(500))

        def chat = Chat.ChatMsg.newBuilder()
                .setId(111111)
                .setFromId("123")
                .setDestId("456")
                .setDestType(Chat.ChatMsg.DestType.SINGLE)
                .setCreateTime(System.currentTimeMillis())
                .setMsgType(Chat.ChatMsg.MsgType.TEXT)
                .setVersion(MsgVersion.V1.getVersion())
                .setMsgBody(ByteString.copyFrom("hello", CharsetUtil.UTF_8))
                .build()

        int sentCnt = 0
        List<Internal.InternalMsg> receiveMsg = new ArrayList()

        def ack = Internal.InternalMsg.newBuilder()
                .setId(1)
                .setVersion(MsgVersion.V1.getVersion())
                .setFrom(Internal.InternalMsg.Module.CONNECTOR)
                .setDest(Internal.InternalMsg.Module.CLIENT)
                .setCreateTime(System.currentTimeMillis())
                .setMsgType(Internal.InternalMsg.MsgType.ERROR)
                .setMsgBody("111111")
                .build()

        when:
        serverAckWindow.offer(chat.getId(), chat, { m -> sentCnt++ })
                .thenAccept({ m -> receiveMsg.add(m) })
        serverAckWindow.ack(ack)

        then:
        sentCnt == 1
        receiveMsg.size() == 1
        receiveMsg.get(0) == ack
    }

    void testTimeout() {
        given:
        def serverAckWindow = new ServerAckWindow("1", 5, Duration.ofMillis(50))

        def chat = Chat.ChatMsg.newBuilder()
                .setId(111111)
                .setFromId("123")
                .setDestId("456")
                .setDestType(Chat.ChatMsg.DestType.SINGLE)
                .setCreateTime(System.currentTimeMillis())
                .setMsgType(Chat.ChatMsg.MsgType.TEXT)
                .setVersion(MsgVersion.V1.getVersion())
                .setMsgBody(ByteString.copyFrom("hello", CharsetUtil.UTF_8))
                .build()

        int sentCnt = 0
        List<Internal.InternalMsg> receiveMsg = new ArrayList()

        def ack = Internal.InternalMsg.newBuilder()
                .setId(1)
                .setVersion(MsgVersion.V1.getVersion())
                .setFrom(Internal.InternalMsg.Module.CONNECTOR)
                .setDest(Internal.InternalMsg.Module.CLIENT)
                .setCreateTime(System.currentTimeMillis())
                .setMsgType(Internal.InternalMsg.MsgType.ERROR)
                .setMsgBody("111111")
                .build()

        when:
        serverAckWindow.offer(chat.getId(), chat, { m -> sentCnt++ })
                .thenAccept({ m -> receiveMsg.add(m) })

        Thread.sleep(155)

        serverAckWindow.ack(ack)

        then:
        sentCnt > 1
        receiveMsg.size() == 1
    }

    void testFull() {
        given:
        def serverAckWindow = new ServerAckWindow("1", 2, Duration.ofMillis(100))

        def chat1 = Chat.ChatMsg.newBuilder()
                .setId(1)
                .setFromId("123")
                .setDestId("456")
                .setDestType(Chat.ChatMsg.DestType.SINGLE)
                .setCreateTime(System.currentTimeMillis())
                .setMsgType(Chat.ChatMsg.MsgType.TEXT)
                .setVersion(MsgVersion.V1.getVersion())
                .setMsgBody(ByteString.copyFrom("hello", CharsetUtil.UTF_8))
                .build()

        def chat2 = Chat.ChatMsg.newBuilder().mergeFrom(chat1)
                .setId(2).build()
        def chat3 = Chat.ChatMsg.newBuilder().mergeFrom(chat1)
                .setId(3).build()
        def chat4 = Chat.ChatMsg.newBuilder().mergeFrom(chat1)
                .setId(4).build()

        def ack1 = Internal.InternalMsg.newBuilder()
                .setId(1)
                .setVersion(MsgVersion.V1.getVersion())
                .setFrom(Internal.InternalMsg.Module.CONNECTOR)
                .setDest(Internal.InternalMsg.Module.CLIENT)
                .setCreateTime(System.currentTimeMillis())
                .setMsgType(Internal.InternalMsg.MsgType.ERROR)
                .setMsgBody(chat1.getId() + "")
                .build()

        def ack2 = Internal.InternalMsg.newBuilder().mergeFrom(ack1)
                .setMsgBody(chat2.getId() + "").build()

        int sentCnt = 0
        List<Internal.InternalMsg> receiveMsg = new ArrayList()
        int exceptionCnt = 0

        def getException = { e ->
            exceptionCnt++
            return null
        }
        when:
        def f1 = serverAckWindow.offer(chat1.getId(), chat1, { m -> sentCnt++ })
                .thenAccept({ m -> receiveMsg.add(m) })
                .exceptionally(getException)
        def f2 = serverAckWindow.offer(chat2.getId(), chat2, { m -> sentCnt++ })
                .thenAccept({ m -> receiveMsg.add(m) })
                .exceptionally(getException)
        serverAckWindow.offer(chat3.getId(), chat3, { m -> sentCnt++ })
                .thenAccept({ m -> receiveMsg.add(m) })
                .exceptionally(getException)
        serverAckWindow.offer(chat4.getId(), chat4, { m -> sentCnt++ })
                .thenAccept({ m -> receiveMsg.add(m) })
                .exceptionally(getException)

        serverAckWindow.ack(ack1)
        serverAckWindow.ack(ack2)

        f1.get()
        f2.get()

        then:
        sentCnt == 2
        receiveMsg.size() == 2
        exceptionCnt == 2
    }

    void testSendRepeat() {
        given:
        def serverAckWindow = new ServerAckWindow("1", 2, Duration.ofMillis(100))

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

        def ack = Internal.InternalMsg.newBuilder()
                .setId(1)
                .setVersion(MsgVersion.V1.getVersion())
                .setFrom(Internal.InternalMsg.Module.CONNECTOR)
                .setDest(Internal.InternalMsg.Module.CLIENT)
                .setCreateTime(System.currentTimeMillis())
                .setMsgType(Internal.InternalMsg.MsgType.ERROR)
                .setMsgBody(chat.getId() + "")
                .build()

        int sentCnt = 0
        List<Internal.InternalMsg> receiveMsg = new ArrayList()
        int exceptionCnt = 0

        def getException = { e ->
            exceptionCnt++
            return null
        }
        when:
        def f1 = serverAckWindow.offer(chat.getId(), chat, { m -> sentCnt++ })
                .thenAccept({ m -> receiveMsg.add(m) })
                .exceptionally(getException)
        serverAckWindow.offer(chat.getId(), chat, { m -> sentCnt++ })
                .thenAccept({ m -> receiveMsg.add(m) })
                .exceptionally(getException)
        serverAckWindow.offer(chat.getId(), chat, { m -> sentCnt++ })
                .thenAccept({ m -> receiveMsg.add(m) })
                .exceptionally(getException)

        serverAckWindow.ack(ack)

        f1.get()

        then:
        sentCnt == 1
        receiveMsg.size() == 1
        exceptionCnt == 2
    }
}
