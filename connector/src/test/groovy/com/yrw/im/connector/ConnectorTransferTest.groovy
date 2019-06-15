package com.yrw.im.connector

import com.google.protobuf.ByteString
import com.yrw.im.common.code.MsgDecoder
import com.yrw.im.common.code.MsgEncoder
import com.yrw.im.common.domain.ResponseCollector
import com.yrw.im.common.domain.conn.Conn
import com.yrw.im.common.parse.ParseService
import com.yrw.im.common.util.IdWorker
import com.yrw.im.connector.domain.ClientConnContext
import com.yrw.im.connector.handler.ConnectorTransferHandler
import com.yrw.im.connector.service.ConnectorService
import com.yrw.im.connector.service.OfflineService
import com.yrw.im.connector.service.UserStatusService
import com.yrw.im.connector.service.rest.ConnectorRestService
import com.yrw.im.connector.start.ConnectorClient
import com.yrw.im.proto.generate.Ack
import com.yrw.im.proto.generate.Chat
import com.yrw.im.proto.generate.Internal
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.util.Attribute
import org.junit.runner.RunWith
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
 * Time: 14:39
 * @author yrw
 */
@PowerMockIgnore(["com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"])
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(Sputnik.class)
@PrepareForTest(ConnectorTransferHandler.class)
class ConnectorTransferTest extends Specification {

    @Shared
    def channel = new EmbeddedChannel()
    @Shared
    def clientConnContext = ConnectorClient.injector.getInstance(ClientConnContext.class)
    @Shared
    def connectorRestService = Mock(ConnectorRestService)
    @Shared
    def userStatusService = new UserStatusService(new OfflineService(connectorRestService, new ParseService()), clientConnContext)

    def setupSpec() {
        channel.pipeline()
                .addLast("MsgDecoder", ConnectorClient.injector.getInstance(MsgDecoder.class))
                .addLast("MsgEncoder", ConnectorClient.injector.getInstance(MsgEncoder.class))
                .addLast("ConnectorTransferHandler", new ConnectorTransferHandler(new ConnectorService(clientConnContext), userStatusService))
    }

    def cleanup() {
        clientConnContext.removeAllConn()
    }

    def "test get chat"() {
        //online
        given:
        def connectorTransferCtx = Mock(ChannelHandlerContext)
        PowerMockito.mockStatic(ConnectorTransferHandler.class)
        when(ConnectorTransferHandler.getCtx()).thenReturn(connectorTransferCtx)
        when(ConnectorTransferHandler.createUserStatusMsgCollector(Duration.ofSeconds(10))).thenReturn(Mock(ResponseCollector) {
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
        userStatusService.userOnline(111112, 456, ctx)

        Chat.ChatMsg chat = Chat.ChatMsg.newBuilder()
                .setVersion(1)
                .setId(IdWorker.genId())
                .setCreateTime(System.currentTimeMillis())
                .setFromId(123)
                .setDestId(456)
                .setMsgType(Chat.ChatMsg.MsgType.TEXT)
                .setMsgBody(ByteString.copyFromUtf8("encodedMsg"))
                .setDestType(Chat.ChatMsg.DestType.SINGLE)
                .setToken("token")
                .build()

        when:
        channel.writeInbound(chat)

        then:
        1 * ctx.writeAndFlush(chat)

        //todo: offline
    }

    def "test get ack"() {
        //online
        given:
        def connectorTransferCtx = Mock(ChannelHandlerContext)
        PowerMockito.mockStatic(ConnectorTransferHandler.class)
        when(ConnectorTransferHandler.getCtx()).thenReturn(connectorTransferCtx)
        when(ConnectorTransferHandler.createUserStatusMsgCollector(Duration.ofSeconds(10))).thenReturn(Mock(ResponseCollector) {
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
        userStatusService.userOnline(111112, 456, ctx)

        Ack.AckMsg delivered = Ack.AckMsg.newBuilder()
                .setVersion(1)
                .setId(IdWorker.genId())
                .setCreateTime(System.currentTimeMillis())
                .setFromId(123)
                .setDestId(456)
                .setMsgType(Ack.AckMsg.MsgType.DELIVERED)
                .setAckMsgId(11241244)
                .setDestType(Ack.AckMsg.DestType.SINGLE)
                .build()

        when:
        channel.writeInbound(delivered)

        then:
        1 * ctx.writeAndFlush(delivered)

        //offline
        when:
        clientConnContext.removeAllConn()
        channel.writeInbound(delivered)

        then:
        0 * ctx.writeAndFlush(_ as Internal.InternalMsg)
    }

    def "test get internal force offline"() {
        given:
        def connectorTransferCtx = Mock(ChannelHandlerContext)
        PowerMockito.mockStatic(ConnectorTransferHandler.class)
        when(ConnectorTransferHandler.getCtx()).thenReturn(connectorTransferCtx)
        when(ConnectorTransferHandler.createUserStatusMsgCollector(Duration.ofSeconds(10))).thenReturn(Mock(ResponseCollector) {
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
        userStatusService.userOnline(11112, 123, ctx)

        when:
        Internal.InternalMsg offline = Internal.InternalMsg.newBuilder()
                .setId(IdWorker.genId())
                .setVersion(1)
                .setCreateTime(System.currentTimeMillis())
                .setFrom(Internal.InternalMsg.Module.TRANSFER)
                .setDest(Internal.InternalMsg.Module.CONNECTOR)
                .setMsgType(Internal.InternalMsg.MsgType.FORCE_OFFLINE)
                .setMsgBody("123")
                .build()
        channel.writeInbound(offline)

        then:
        clientConnContext.getConnByUserId(123) == null
        clientConnContext.getConn(ctx) == null
    }

    def "test get internal ack"() {
        given:
        def result = new ArrayList<Internal.InternalMsg>()
        def responseCollector = ConnectorTransferHandler.createUserStatusMsgCollector(Duration.ofSeconds(2))

        when:
        responseCollector.getFuture().whenComplete({ r, e -> result.add(r) })
        Internal.InternalMsg ack = Internal.InternalMsg.newBuilder()
                .setVersion(1)
                .setId(IdWorker.genId())
                .setCreateTime(System.currentTimeMillis())
                .setFrom(Internal.InternalMsg.Module.TRANSFER)
                .setDest(Internal.InternalMsg.Module.CONNECTOR)
                .setMsgType(Internal.InternalMsg.MsgType.ACK)
                .setMsgBody("1111112")
                .build()

        channel.writeInbound(ack)

        then:
        result.size() == 1
        responseCollector.getFuture().isDone()
    }
}
