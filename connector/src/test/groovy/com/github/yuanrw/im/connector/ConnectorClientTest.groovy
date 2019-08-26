package com.github.yuanrw.im.connector

import com.github.yuanrw.im.common.code.MsgDecoder
import com.github.yuanrw.im.common.code.MsgEncoder
import com.github.yuanrw.im.common.domain.ResponseCollector
import com.github.yuanrw.im.common.domain.conn.Conn
import com.github.yuanrw.im.common.parse.ParseService
import com.github.yuanrw.im.common.util.IdWorker
import com.github.yuanrw.im.connector.config.ConnectorRestServiceFactory
import com.github.yuanrw.im.connector.domain.ClientConnContext
import com.github.yuanrw.im.connector.handler.ConnectorClientHandler
import com.github.yuanrw.im.connector.handler.ConnectorTransferHandler
import com.github.yuanrw.im.connector.service.ConnectorService
import com.github.yuanrw.im.connector.service.OfflineService
import com.github.yuanrw.im.connector.service.UserStatusService
import com.github.yuanrw.im.connector.service.rest.ConnectorRestService
import com.github.yuanrw.im.connector.start.ConnectorStarter
import com.github.yuanrw.im.protobuf.generate.Ack
import com.github.yuanrw.im.protobuf.generate.Chat
import com.github.yuanrw.im.protobuf.generate.Internal
import com.google.common.collect.Lists
import com.google.protobuf.ByteString
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.util.Attribute
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PowerMockIgnore
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import org.powermock.modules.junit4.PowerMockRunnerDelegate
import org.spockframework.runtime.Sputnik
import spock.lang.Shared
import spock.lang.Specification

import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.function.BiConsumer

import static org.powermock.api.mockito.PowerMockito.when

/**
 * Date: 2019-06-06
 * Time: 14:38
 * @author yrw
 */
@PowerMockIgnore(["com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"])
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(Sputnik.class)
@PrepareForTest(ConnectorTransferHandler.class)
class ConnectorClientTest extends Specification {

    @Shared
    def ch = new EmbeddedChannel()
    @Shared
    def connectorRestService = Mock(ConnectorRestService)
    @Shared
    def clientConnContext = ConnectorStarter.injector.getInstance(ClientConnContext.class)
    @Shared
    def connectorRestServiceFactory = Mock(ConnectorRestServiceFactory) {
        createService() >> connectorRestService
    }
    @Shared
    def userStatusService = new UserStatusService(new OfflineService(
            connectorRestServiceFactory, new ParseService()),
            clientConnContext, new ConnectorService())

    def setupSpec() {
        ch.pipeline()
                .addLast("MsgDecoder", ConnectorStarter.injector.getInstance(MsgDecoder.class))
                .addLast("MsgEncoder", ConnectorStarter.injector.getInstance(MsgEncoder.class))
                .addLast("ConnectorClientHandler", new ConnectorClientHandler(new ConnectorService(clientConnContext), userStatusService, clientConnContext))
    }

    def cleanup() {
        clientConnContext.removeAllConn()
    }

    def "test get internal greet"() {
        given:
        def connectorTransferCtx = Mock(ChannelHandlerContext)
        PowerMockito.mockStatic(ConnectorTransferHandler.class)
        when(ConnectorTransferHandler.getCtxList()).thenReturn(Lists.newArrayList(connectorTransferCtx))
        when(ConnectorTransferHandler.createUserStatusMsgCollector(Mockito.anyLong(), Mockito.eq(Duration.ofSeconds(10))))
                .thenReturn(Mock(ResponseCollector) {
            getFuture() >> Mock(CompletableFuture) {
                whenComplete(_ as BiConsumer) >> Mock(CompletableFuture)
            }
        })
        when:
        Internal.InternalMsg greet = Internal.InternalMsg.newBuilder()
                .setVersion(1)
                .setId(111112)
                .setCreateTime(System.currentTimeMillis())
                .setFrom(Internal.InternalMsg.Module.CLIENT)
                .setDest(Internal.InternalMsg.Module.CONNECTOR)
                .setMsgType(Internal.InternalMsg.MsgType.GREET)
                .setMsgBody("123")
                .build()
        ch.writeInbound(greet)

        then:
        1 * connectorTransferCtx.writeAndFlush(_ as Internal.InternalMsg)
        clientConnContext.getConnByUserId("123") != null
    }

    def "test get ack"() {
        //online
        given:
        def connectorTransferCtx = Mock(ChannelHandlerContext)
        PowerMockito.mockStatic(ConnectorTransferHandler.class)
        when(ConnectorTransferHandler.getCtxList()).thenReturn(Lists.newArrayList(connectorTransferCtx))
        when(ConnectorTransferHandler.createUserStatusMsgCollector(Mockito.anyLong(), Mockito.eq(Duration.ofSeconds(10))))
                .thenReturn(Mock(ResponseCollector) {
            getFuture() >> Mock(CompletableFuture) {
                whenComplete(_ as BiConsumer) >> Mock(CompletableFuture)
            }
        })

        def map = new HashMap<String, Object>()
        def ctx = Mock(ChannelHandlerContext) {
            channel() >> Mock(Channel) {
                attr(Conn.NET_ID) >> Mock(Attribute) {
                    set() >> { arguments -> map.put("net_id", arguments[0]) }
                    get() >> { map.get("net_id") }
                }
            }
        }
        userStatusService.userOnline(111112, "456", ctx)

        Ack.AckMsg delivered = Ack.AckMsg.newBuilder()
                .setVersion(1)
                .setId(IdWorker.genId())
                .setCreateTime(System.currentTimeMillis())
                .setFromId("123")
                .setDestId("456")
                .setMsgType(Ack.AckMsg.MsgType.DELIVERED)
                .setAckMsgId(11241244)
                .setDestType(Ack.AckMsg.DestType.SINGLE)
                .build()

        when:
        ch.writeInbound(delivered)

        then:
        1 * ctx.writeAndFlush(delivered)
        0 * connectorTransferCtx.write(delivered)

        //offline
        when:
        clientConnContext.removeAllConn()
        ch.writeInbound(delivered)

        then:
        0 * ctx.writeAndFlush(_ as Internal.InternalMsg)
        1 * connectorTransferCtx.writeAndFlush(delivered)
    }

    def "test get chat"() {
        //online
        given:
        def connectorTransferCtx = Mock(ChannelHandlerContext)
        PowerMockito.mockStatic(ConnectorTransferHandler.class)
        when(ConnectorTransferHandler.getCtxList()).thenReturn(Lists.newArrayList(connectorTransferCtx))
        when(ConnectorTransferHandler.createUserStatusMsgCollector(Mockito.anyLong(), Mockito.eq(Duration.ofSeconds(10))))
                .thenReturn(Mock(ResponseCollector) {
            getFuture() >> Mock(CompletableFuture) {
                whenComplete(_ as BiConsumer) >> Mock(CompletableFuture)
            }
        })

        def map = new HashMap<String, Object>()
        def ctx = Mock(ChannelHandlerContext) {
            channel() >> Mock(Channel) {
                attr(Conn.NET_ID) >> Mock(Attribute) {
                    set() >> { arguments -> map.put("net_id", arguments[0]) }
                    get() >> { map.get("net_id") }
                }
            }
        }
        userStatusService.userOnline(111112, "456", ctx)

        Chat.ChatMsg chat = Chat.ChatMsg.newBuilder()
                .setVersion(1)
                .setId(IdWorker.genId())
                .setCreateTime(System.currentTimeMillis())
                .setFromId("123")
                .setDestId("456")
                .setMsgType(Chat.ChatMsg.MsgType.TEXT)
                .setMsgBody(ByteString.copyFromUtf8("encodedMsg"))
                .setDestType(Chat.ChatMsg.DestType.SINGLE)
                .build()

        when:
        ch.writeInbound(chat)

        then:
        1 * ctx.writeAndFlush(chat)
        0 * connectorTransferCtx.writeAndFlush(_ as Chat.ChatMsg)

        //offline
        when:
        clientConnContext.removeAllConn()
        ch.writeInbound(chat)

        then:
        0 * ctx.writeAndFlush(_ as Chat.ChatMsg)
        1 * connectorTransferCtx.writeAndFlush(chat)
    }
}
