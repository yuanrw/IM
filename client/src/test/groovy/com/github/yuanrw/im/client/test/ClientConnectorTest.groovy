package com.github.yuanrw.im.client.test

import com.github.yuanrw.im.client.ImClient
import com.github.yuanrw.im.client.api.ClientMsgListener
import com.github.yuanrw.im.client.context.UserContext
import com.github.yuanrw.im.client.context.impl.MemoryRelationCache
import com.github.yuanrw.im.client.handler.ClientConnectorHandler
import com.github.yuanrw.im.client.handler.code.AesDecoder
import com.github.yuanrw.im.client.handler.code.AesEncoder
import com.github.yuanrw.im.common.code.MsgDecoder
import com.github.yuanrw.im.common.code.MsgEncoder
import com.github.yuanrw.im.common.domain.ack.ClientAckWindow
import com.github.yuanrw.im.common.domain.ack.ServerAckWindow
import com.github.yuanrw.im.common.domain.constant.MsgVersion
import com.github.yuanrw.im.common.domain.po.RelationDetail
import com.github.yuanrw.im.common.util.Encryption
import com.github.yuanrw.im.protobuf.generate.Ack
import com.github.yuanrw.im.protobuf.generate.Chat
import com.github.yuanrw.im.protobuf.generate.Internal
import com.google.protobuf.ByteString
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.util.CharsetUtil
import spock.lang.Specification

/**
 * Date: 2019-06-06
 * Time: 14:30
 * @author yrw
 */
class ClientConnectorTest extends Specification {

    def "test get ack"() {
        given:
        def c = new EmbeddedChannel()
        def clientMsgListener = Mock(ClientMsgListener)

        def handler = new ClientConnectorHandler(clientMsgListener)
        def ctx = Mock(ChannelHandlerContext) {
            channel() >> Mock(Channel) {
                isOpen() >> true
            }
        }
        handler.setCtx(ctx)

        def clientAckWindow = new ClientAckWindow(5)
        handler.setClientAckWindow(clientAckWindow)

        c.pipeline()
                .addLast("MsgEncoder", new MsgEncoder())
                .addLast("AesEncoder", new AesEncoder(new UserContext()))
                .addLast("MsgDecoder", new ImClient("localhost", 9082, "http://127.0.0.1:8080")
                .injector.getInstance(MsgDecoder.class))
                .addLast("AesDecoder", new AesDecoder())
                .addLast("ClientConnectorHandler", handler)

        when:
        def delivered = Ack.AckMsg.newBuilder()
                .setVersion(MsgVersion.V1.getVersion())
                .setId(1)
                .setCreateTime(System.currentTimeMillis())
                .setFromId("123")
                .setDestId("456")
                .setMsgType(Ack.AckMsg.MsgType.DELIVERED)
                .setDestType(Ack.AckMsg.DestType.SINGLE)
                .setAckMsgId(111112)
                .build()

        c.writeInbound(delivered)
        Thread.sleep(40)

        then:
        1 * clientMsgListener.hasDelivered(111112)
        //send ack
        1 * ctx.writeAndFlush(_ as Internal.InternalMsg)

        when:
        def read = Ack.AckMsg.newBuilder()
                .mergeFrom(delivered)
                .setId(2)
                .setMsgType(Ack.AckMsg.MsgType.READ)
                .build()

        c.writeInbound(read)
        Thread.sleep(40)

        then:
        1 * clientMsgListener.hasRead(111112)
        //send ack
        1 * ctx.writeAndFlush(_ as Internal.InternalMsg)
    }

    def "test get internal"() {
        given:
        def channel = new EmbeddedChannel()
        def handler = new ClientConnectorHandler(Mock(ClientMsgListener))

        def serverAckWindow = Mock(ServerAckWindow)
        def clientAckWindow = new ClientAckWindow(5)
        handler.setClientAckWindow(clientAckWindow)
        handler.setServerAckWindow(serverAckWindow)

        channel.pipeline()
                .addLast("MsgEncoder", new MsgEncoder())
                .addLast("AesEncoder", new AesEncoder(new UserContext()))
                .addLast("MsgDecoder", new MsgDecoder())
                .addLast("AesDecoder", new AesEncoder(new UserContext()))
                .addLast("ClientConnectorHandler", handler)

        def internal = Internal.InternalMsg.newBuilder()
                .setVersion(MsgVersion.V1.getVersion())
                .setId(1)
                .setCreateTime(System.currentTimeMillis())
                .setFrom(Internal.InternalMsg.Module.CONNECTOR)
                .setDest(Internal.InternalMsg.Module.CLIENT)
                .setMsgType(Internal.InternalMsg.MsgType.ACK)
                .setMsgBody("1111112")
                .build()

        when:
        channel.writeInbound(internal)

        then:
        1 * serverAckWindow.ack(internal)
        0 * _
    }

    def "test get chat"() {
        given:
        def clientMsgListener = Mock(ClientMsgListener)

        def c = new EmbeddedChannel()

        def r = new RelationDetail()
        r.setUserId1("123")
        r.setUserId2("456")
        r.setEncryptKey("HvxZFa7B1dBlKwP7|9302073163544974")

        def userContext = new UserContext(new MemoryRelationCache())
        userContext.addRelation(r)

        String[] keys = r.getEncryptKey().split("\\|")
        byte[] encodeBody = Encryption.encrypt(keys[0], keys[1], "hello".getBytes(CharsetUtil.UTF_8))

        def ctx = Mock(ChannelHandlerContext) {
            channel() >> Mock(Channel) {
                isOpen() >> true
            }
        }
        def handler = new ClientConnectorHandler(clientMsgListener)
        handler.setCtx(ctx)

        def clientAckWindow = new ClientAckWindow(5)
        handler.setClientAckWindow(clientAckWindow)
        c.pipeline()
                .addLast("MsgEncoder", new MsgEncoder())
                .addLast("AesEncoder", new AesEncoder(userContext))
                .addLast("MsgDecoder", new ImClient("localhost", 9082, "http://127.0.0.1:8080")
                .injector.getInstance(MsgDecoder.class))
                .addLast("AesDecoder", new AesDecoder(userContext))
                .addLast("ClientConnectorHandler", handler)

        def chat = Chat.ChatMsg.newBuilder()
                .setVersion(MsgVersion.V1.getVersion())
                .setId(1)
                .setCreateTime(System.currentTimeMillis())
                .setFromId("123")
                .setDestId("456")
                .setMsgType(Chat.ChatMsg.MsgType.TEXT)
                .setDestType(Chat.ChatMsg.DestType.SINGLE)
                .setMsgBody(ByteString.copyFrom(encodeBody))
                .build()

        def decodedChat = Chat.ChatMsg.newBuilder().mergeFrom(chat)
                .setMsgBody(ByteString.copyFromUtf8("hello")).build()
        when:
        c.writeInbound(chat)
        Thread.sleep(40)

        then:
        1 * clientMsgListener.read(decodedChat)
        1 * ctx.writeAndFlush(_ as Internal.InternalMsg)
    }
}
