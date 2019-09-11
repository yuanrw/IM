package com.github.yuanrw.im.common.test

import com.github.yuanrw.im.common.domain.ack.ClientAckWindow
import com.github.yuanrw.im.common.domain.constant.MsgVersion
import com.github.yuanrw.im.protobuf.generate.Chat
import com.github.yuanrw.im.protobuf.generate.Internal
import com.google.protobuf.ByteString
import com.google.protobuf.Message
import io.netty.channel.ChannelHandlerContext
import io.netty.util.CharsetUtil
import spock.lang.Specification

/**
 * Date: 2019-09-08
 * Time: 23:40
 * @author yrw
 */
class ClientAckWindowTest extends Specification {

    /**
     * 按序处理
     */
    void testProcessWell() {
        given:
        def ctx = Mock(ChannelHandlerContext)
        def clientAckWindow = new ClientAckWindow(3, ctx)

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
        def chat2 = Chat.ChatMsg.newBuilder()
                .mergeFrom(chat1)
                .setId(2)
                .build()
        def chat3 = Chat.ChatMsg.newBuilder()
                .mergeFrom(chat1)
                .setId(3)
                .build()


        List<Message> processMsg = new ArrayList()

        when:
        def f1 = clientAckWindow.offer(chat1.getId(), Internal.InternalMsg.Module.CLIENT,
                Internal.InternalMsg.Module.CONNECTOR, chat1, { m -> processMsg.add(m) })
        def f2 = clientAckWindow.offer(chat2.getId(), Internal.InternalMsg.Module.CLIENT,
                Internal.InternalMsg.Module.CONNECTOR, chat2, { m -> processMsg.add(m) })
        def f3 = clientAckWindow.offer(chat3.getId(), Internal.InternalMsg.Module.CLIENT,
                Internal.InternalMsg.Module.CONNECTOR, chat3, { m -> processMsg.add(m) })

        f1.get()
        f2.get()
        f3.get()

        then:
        processMsg.size() == 3
        3 * ctx.writeAndFlush(_ as Internal.InternalMsg)
    }

    /**
     * 按序处理，个别任务时间过长
     */
    void testProcessLongTime() {
        given:
        def ctx = Mock(ChannelHandlerContext)
        def clientAckWindow = new ClientAckWindow(3, ctx)

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
        def chat2 = Chat.ChatMsg.newBuilder()
                .mergeFrom(chat1)
                .setId(2)
                .build()
        def chat3 = Chat.ChatMsg.newBuilder()
                .mergeFrom(chat1)
                .setId(3)
                .build()

        List<Message> processMsg = new ArrayList()
        def longProcess = { m ->
            Thread.sleep(500)
            processMsg.add(m)
        }

        when:
        def f1 = clientAckWindow.offer(chat1.getId(), Internal.InternalMsg.Module.CLIENT,
                Internal.InternalMsg.Module.CONNECTOR, chat1, { m -> processMsg.add(m) })
        def f2 = clientAckWindow.offer(chat2.getId(), Internal.InternalMsg.Module.CLIENT,
                Internal.InternalMsg.Module.CONNECTOR, chat2, longProcess)
        def f3 = clientAckWindow.offer(chat3.getId(), Internal.InternalMsg.Module.CLIENT,
                Internal.InternalMsg.Module.CONNECTOR, chat3, { m -> processMsg.add(m) })

        f1.get()
        f2.get()
        f3.get()

        then:
        processMsg.size() == 3
        3 * ctx.writeAndFlush(_ as Internal.InternalMsg)
    }

    /**
     * 按序处理
     */
    void testLongTimeNotFull() {
        given:
        def ctx = Mock(ChannelHandlerContext)
        def clientAckWindow = new ClientAckWindow(1, ctx)

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
        def chat2 = Chat.ChatMsg.newBuilder()
                .mergeFrom(chat1)
                .setId(2)
                .build()
        def chat3 = Chat.ChatMsg.newBuilder()
                .mergeFrom(chat1)
                .setId(3)
                .build()


        List<Message> processMsg = new ArrayList()
        def longProcess = { m ->
            Thread.sleep(50)
            processMsg.add(m)
        }

        when:
        def f1 = clientAckWindow.offer(chat1.getId(), Internal.InternalMsg.Module.CLIENT,
                Internal.InternalMsg.Module.CONNECTOR, chat1, longProcess)

        f1.get()

        def f2 = clientAckWindow.offer(chat2.getId(), Internal.InternalMsg.Module.CLIENT,
                Internal.InternalMsg.Module.CONNECTOR, chat2, longProcess)

        f2.get()

        def f3 = clientAckWindow.offer(chat3.getId(), Internal.InternalMsg.Module.CLIENT,
                Internal.InternalMsg.Module.CONNECTOR, chat3, longProcess)

        f3.get()

        then:
        f1.isDone()
        f2.isDone()
        f3.isDone()

        processMsg.size() == 3
        3 * ctx.writeAndFlush(_ as Internal.InternalMsg)
    }

    /**
     * 队列满
     */
    void testFull() {
        given:
        def ctx = Mock(ChannelHandlerContext)
        def clientAckWindow = new ClientAckWindow(2, ctx)

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
        def chat2 = Chat.ChatMsg.newBuilder()
                .mergeFrom(chat1)
                .setId(2)
                .build()
        def chat3 = Chat.ChatMsg.newBuilder()
                .mergeFrom(chat1)
                .setId(3)
                .build()

        List<Message> processMsg = new ArrayList()
        def longProcess = { m ->
            Thread.sleep(100)
            processMsg.add(m)
        }

        when:
        def f1 = clientAckWindow.offer(chat1.getId(), Internal.InternalMsg.Module.CLIENT,
                Internal.InternalMsg.Module.CONNECTOR, chat1, longProcess)
        def f2 = clientAckWindow.offer(chat2.getId(), Internal.InternalMsg.Module.CLIENT,
                Internal.InternalMsg.Module.CONNECTOR, chat2, longProcess)
        def f3 = clientAckWindow.offer(chat3.getId(), Internal.InternalMsg.Module.CLIENT,
                Internal.InternalMsg.Module.CONNECTOR, chat3, longProcess)

        then:
        f1 != null
        f2 != null
        f3 == null
    }
}
