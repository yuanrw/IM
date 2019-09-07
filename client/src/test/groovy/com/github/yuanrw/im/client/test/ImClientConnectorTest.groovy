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
import com.github.yuanrw.im.common.domain.constant.MsgVersion
import com.github.yuanrw.im.common.domain.po.RelationDetail
import com.github.yuanrw.im.common.util.Encryption
import com.github.yuanrw.im.common.util.IdWorker
import com.github.yuanrw.im.protobuf.generate.Ack
import com.github.yuanrw.im.protobuf.generate.Chat
import com.github.yuanrw.im.protobuf.generate.Internal
import com.google.protobuf.ByteString
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.util.CharsetUtil
import spock.lang.Specification

import java.time.Duration

/**
 * Date: 2019-06-06
 * Time: 14:30
 * @author yrw
 */
class ImClientConnectorTest extends Specification {

    def "test get ack"() {
        given:
        def channel = new EmbeddedChannel()
        def clientMsgListener = Mock(ClientMsgListener)
        channel.pipeline()
                .addLast("MsgEncoder", new MsgEncoder())
                .addLast("AesEncoder", new AesEncoder(new UserContext()))
                .addLast("MsgDecoder", new ImClient("localhost", 9082, "http://127.0.0.1:8080")
                .injector.getInstance(MsgDecoder.class))
                .addLast("AesDecoder", new AesDecoder())
                .addLast("ClientConnectorHandler", new ClientConnectorHandler(clientMsgListener))

        when:
        def delivered = Ack.AckMsg.newBuilder()
                .setVersion(MsgVersion.V1.getVersion())
                .setId(IdWorker.genId())
                .setCreateTime(System.currentTimeMillis())
                .setFromId("123")
                .setDestId("456")
                .setMsgType(Ack.AckMsg.MsgType.DELIVERED)
                .setDestType(Ack.AckMsg.DestType.SINGLE)
                .setAckMsgId(1111112)
                .build()

        channel.writeInbound(delivered)

        then:
        1 * clientMsgListener.hasDelivered(1111112)
        0 * _

        when:
        def read = Ack.AckMsg.newBuilder()
                .mergeFrom(delivered)
                .setMsgType(Ack.AckMsg.MsgType.READ)
                .build()

        channel.writeInbound(read)

        then:
        1 * clientMsgListener.hasRead(1111112)
        0 * _
    }

    def "test get internal"() {
        given:
        def channel = new EmbeddedChannel()
        def handler = new ClientConnectorHandler(Mock(ClientMsgListener))
        channel.pipeline()
                .addLast("MsgEncoder", new MsgEncoder())
                .addLast("AesEncoder", new AesEncoder(new UserContext()))
                .addLast("MsgDecoder", new MsgDecoder())
                .addLast("AesDecoder", new AesEncoder(new UserContext()))
                .addLast("ClientConnectorHandler", handler)

        def collector = handler.createCollector(Duration.ofSeconds(2))

        def internal = Internal.InternalMsg.newBuilder()
                .setVersion(MsgVersion.V1.getVersion())
                .setId(IdWorker.genId())
                .setCreateTime(System.currentTimeMillis())
                .setFrom(Internal.InternalMsg.Module.CONNECTOR)
                .setDest(Internal.InternalMsg.Module.CLIENT)
                .setMsgType(Internal.InternalMsg.MsgType.ACK)
                .setMsgBody("1111112")
                .build()

        when:
        channel.writeInbound(internal)

        then:
        collector.getFuture().isDone()
        0 * _
    }

    def "test get chat"() {
        given:
        def clientMsgListener = Mock(ClientMsgListener)

        def channel = new EmbeddedChannel()

        def r = new RelationDetail()
        r.setUserId1("123")
        r.setUserId2("456")
        r.setEncryptKey("HvxZFa7B1dBlKwP7|9302073163544974")

        def userContext = new UserContext(new MemoryRelationCache())
        userContext.addRelation(r)

        String[] keys = r.getEncryptKey().split("\\|")
        byte[] encodeBody = Encryption.encrypt(keys[0], keys[1], "hello".getBytes(CharsetUtil.UTF_8))

        channel.pipeline()
                .addLast("MsgEncoder", new MsgEncoder())
                .addLast("AesEncoder", new AesEncoder(userContext))
                .addLast("MsgDecoder", new ImClient("localhost", 9082, "http://127.0.0.1:8080")
                .injector.getInstance(MsgDecoder.class))
                .addLast("AesDecoder", new AesDecoder(userContext))
                .addLast("ClientConnectorHandler", new ClientConnectorHandler(clientMsgListener))

        def chat = Chat.ChatMsg.newBuilder()
                .setVersion(MsgVersion.V1.getVersion())
                .setId(IdWorker.genId())
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
        channel.writeInbound(chat)

        then:
        1 * clientMsgListener.read(decodedChat)
    }
}
