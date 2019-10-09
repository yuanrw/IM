package com.github.yuanrw.im.connector

import com.github.yuanrw.im.common.code.MsgDecoder
import com.github.yuanrw.im.common.code.MsgEncoder
import com.github.yuanrw.im.common.domain.ack.ClientAckWindow
import com.github.yuanrw.im.common.domain.ack.ServerAckWindow
import com.github.yuanrw.im.common.domain.conn.Conn
import com.github.yuanrw.im.common.domain.constant.MsgVersion
import com.github.yuanrw.im.common.domain.po.Offline
import com.github.yuanrw.im.common.parse.ParseService
import com.github.yuanrw.im.common.util.IdWorker
import com.github.yuanrw.im.connector.config.ConnectorRestServiceFactory
import com.github.yuanrw.im.connector.domain.ClientConnContext
import com.github.yuanrw.im.connector.handler.ConnectorClientHandler
import com.github.yuanrw.im.connector.handler.ConnectorTransferHandler
import com.github.yuanrw.im.connector.service.ConnectorToClientService
import com.github.yuanrw.im.connector.service.OfflineService
import com.github.yuanrw.im.connector.service.UserOnlineService
import com.github.yuanrw.im.connector.service.rest.ConnectorRestService
import com.github.yuanrw.im.connector.start.ConnectorStarter
import com.github.yuanrw.im.protobuf.generate.Ack
import com.github.yuanrw.im.protobuf.generate.Chat
import com.github.yuanrw.im.protobuf.generate.Internal
import com.github.yuanrw.im.user.status.factory.UserStatusServiceFactory
import com.github.yuanrw.im.user.status.service.impl.MemoryUserStatusServiceImpl
import com.google.common.collect.Lists
import com.google.protobuf.ByteString
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

import static org.mockito.ArgumentMatchers.anyLong
import static org.powermock.api.mockito.PowerMockito.when

/**
 * Date: 2019-06-06
 * Time: 21:38
 * @author yrw
 */
@PowerMockIgnore(["com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"])
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(Sputnik.class)
@PrepareForTest(ConnectorTransferHandler.class)
class ConnectorClientTest extends Specification {

    @Shared
    def clientConnContext = new ClientConnContext()
    @Shared
    UserOnlineService userOnlineService

    def setupSpec() {
        ConnectorStarter.CONNECTOR_CONFIG.setRedisHost("redisHost")
        ConnectorStarter.CONNECTOR_CONFIG.setRedisPort(123)
        ConnectorStarter.CONNECTOR_CONFIG.setRedisPassword("redisPassword")
        ConnectorStarter.CONNECTOR_CONFIG.setRestUrl("restUrl")

        def connectorRestService = Mock(ConnectorRestService) {
            offlines(_ as String) >> new ArrayList<Offline>()
        }
        def connectorRestServiceFactory = Mock(ConnectorRestServiceFactory) {
            createService(_ as String) >> connectorRestService
        }
        def userStatusServiceFactory = Mock(UserStatusServiceFactory) {
            createService(_ as Properties) >> new MemoryUserStatusServiceImpl()
        }
        userOnlineService = new UserOnlineService(new OfflineService(
                connectorRestServiceFactory, new ParseService()),
                clientConnContext, new ConnectorToClientService(), userStatusServiceFactory)
    }

    def cleanup() {
        clientConnContext.removeAllConn()
    }

    def "test get internal greet"() {
        given:
        def handler = new ConnectorClientHandler(new ConnectorToClientService(clientConnContext),
                userOnlineService, clientConnContext)
        handler.setClientAckWindow(new ClientAckWindow(5))

        def ch = new EmbeddedChannel()
        ch.pipeline()
                .addLast("MsgDecoder", ConnectorStarter.injector.getInstance(MsgDecoder.class))
                .addLast("MsgEncoder", ConnectorStarter.injector.getInstance(MsgEncoder.class))
                .addLast("ConnectorClientHandler", handler)

        def connectorTransferCtx = Mock(ChannelHandlerContext)
        PowerMockito.mockStatic(ConnectorTransferHandler.class)
        when(ConnectorTransferHandler.getCtxList()).thenReturn(Lists.newArrayList(connectorTransferCtx))
        when:
        //user online
        Internal.InternalMsg greet = Internal.InternalMsg.newBuilder()
                .setVersion(MsgVersion.V1.getVersion())
                .setId(111112)
                .setCreateTime(System.currentTimeMillis())
                .setFrom(Internal.InternalMsg.Module.CLIENT)
                .setDest(Internal.InternalMsg.Module.CONNECTOR)
                .setMsgType(Internal.InternalMsg.MsgType.GREET)
                .setMsgBody("123")
                .build()
        ch.writeInbound(greet)

        Thread.sleep(40)

        then:
        //get conn in memory
        clientConnContext.getConnByUserId("123") != null
    }

    def "test get ack online"() {
        given:
        def handler = new ConnectorClientHandler(new ConnectorToClientService(clientConnContext),
                userOnlineService, clientConnContext)
        handler.setClientAckWindow(new ClientAckWindow(5))

        def ch = new EmbeddedChannel()
        ch.pipeline()
                .addLast("MsgDecoder", ConnectorStarter.injector.getInstance(MsgDecoder.class))
                .addLast("MsgEncoder", ConnectorStarter.injector.getInstance(MsgEncoder.class))
                .addLast("ConnectorClientHandler", handler)

        def connectorTransferCtx = Mock(ChannelHandlerContext)
        PowerMockito.mockStatic(ConnectorTransferHandler.class)
        when(ConnectorTransferHandler.getCtxList()).thenReturn(Lists.newArrayList(connectorTransferCtx))

        def map = new HashMap<String, Object>()
        def ctx = Mock(ChannelHandlerContext) {
            channel() >> Mock(Channel) {
                attr(Conn.NET_ID) >> Mock(Attribute) {
                    set() >> { arguments -> map.put("net_id", arguments[0]) }
                    get() >> { map.get("net_id") }
                }
            }
        }

        //make user online
        def conn = userOnlineService.userOnline("456", ctx)
        new ServerAckWindow(conn.getNetId(), 10, Duration.ofSeconds(5))

        Ack.AckMsg delivered = Ack.AckMsg.newBuilder()
                .setVersion(MsgVersion.V1.getVersion())
                .setId(1)
                .setCreateTime(System.currentTimeMillis())
                .setFromId("123")
                .setDestId("456")
                .setMsgType(Ack.AckMsg.MsgType.DELIVERED)
                .setAckMsgId(11241244)
                .setDestType(Ack.AckMsg.DestType.SINGLE)
                .build()

        when:
        ch.writeInbound(delivered)
        Thread.sleep(40)

        then:
        1 * ctx.writeAndFlush(delivered)
        0 * connectorTransferCtx.write(delivered)
    }

    def "test get ack offline"() {
        given:
        def handler = new ConnectorClientHandler(new ConnectorToClientService(clientConnContext),
                userOnlineService, clientConnContext)
        handler.setClientAckWindow(new ClientAckWindow(5))

        def ch = new EmbeddedChannel()
        ch.pipeline()
                .addLast("MsgDecoder", ConnectorStarter.injector.getInstance(MsgDecoder.class))
                .addLast("MsgEncoder", ConnectorStarter.injector.getInstance(MsgEncoder.class))
                .addLast("ConnectorClientHandler", handler)

        def connectorTransferCtx = Mock(ChannelHandlerContext) {
            channel() >> Mock(Channel) {
                attr(Conn.NET_ID) >> Mock(Attribute) {
                    get() >> IdWorker.uuid()
                }
            }
        }

        PowerMockito.mockStatic(ConnectorTransferHandler.class)
        when(ConnectorTransferHandler.getOneOfTransferCtx(anyLong())).thenReturn(connectorTransferCtx)

        Ack.AckMsg delivered = Ack.AckMsg.newBuilder()
                .setVersion(MsgVersion.V1.getVersion())
                .setId(1)
                .setCreateTime(System.currentTimeMillis())
                .setFromId("123")
                .setDestId("456")
                .setMsgType(Ack.AckMsg.MsgType.DELIVERED)
                .setAckMsgId(11241244)
                .setDestType(Ack.AckMsg.DestType.SINGLE)
                .build()

        when:
        ch.writeInbound(delivered)
        Thread.sleep(100)

        then:
        1 * connectorTransferCtx.writeAndFlush(delivered)
    }

    def "test get chat online"() {
        //online
        given:
        def handler = new ConnectorClientHandler(new ConnectorToClientService(clientConnContext),
                userOnlineService, clientConnContext)
        handler.setClientAckWindow(new ClientAckWindow(5))

        def ch = new EmbeddedChannel()
        ch.pipeline()
                .addLast("MsgDecoder", ConnectorStarter.injector.getInstance(MsgDecoder.class))
                .addLast("MsgEncoder", ConnectorStarter.injector.getInstance(MsgEncoder.class))
                .addLast("ConnectorClientHandler", handler)

        def connectorTransferCtx = Mock(ChannelHandlerContext)
        PowerMockito.mockStatic(ConnectorTransferHandler.class)
        when(ConnectorTransferHandler.getOneOfTransferCtx(anyLong())).thenReturn(connectorTransferCtx)

        def map = new HashMap<String, Object>()
        def ctx = Mock(ChannelHandlerContext) {
            channel() >> Mock(Channel) {
                attr(Conn.NET_ID) >> Mock(Attribute) {
                    set() >> { arguments -> map.put("net_id", arguments[0]) }
                    get() >> { map.get("net_id") }
                }
            }
        }
        //make user online
        def conn = userOnlineService.userOnline("456", ctx)
        new ServerAckWindow(conn.getNetId(), 10, Duration.ofSeconds(5))

        Chat.ChatMsg chat = Chat.ChatMsg.newBuilder()
                .setVersion(MsgVersion.V1.getVersion())
                .setId(1)
                .setCreateTime(System.currentTimeMillis())
                .setFromId("123")
                .setDestId("456")
                .setMsgType(Chat.ChatMsg.MsgType.TEXT)
                .setMsgBody(ByteString.copyFromUtf8("encodedMsg"))
                .setDestType(Chat.ChatMsg.DestType.SINGLE)
                .build()

        when:
        ch.writeInbound(chat)
        Thread.sleep(100)

        then:
        1 * ctx.writeAndFlush(chat)
        0 * connectorTransferCtx.writeAndFlush(_ as Chat.ChatMsg)
    }

    def "test get chat offline"() {
        //online
        given:
        def handler = new ConnectorClientHandler(new ConnectorToClientService(clientConnContext),
                userOnlineService, clientConnContext)
        handler.setClientAckWindow(new ClientAckWindow(5))

        def ch = new EmbeddedChannel()
        ch.pipeline()
                .addLast("MsgDecoder", ConnectorStarter.injector.getInstance(MsgDecoder.class))
                .addLast("MsgEncoder", ConnectorStarter.injector.getInstance(MsgEncoder.class))
                .addLast("ConnectorClientHandler", handler)

        def ctx1 = Mock(ChannelHandlerContext) {
            channel() >> Mock(Channel) {
                attr(Conn.NET_ID) >> Mock(Attribute) {
                    get() >> IdWorker.uuid()
                }
            }
        }
        PowerMockito.mockStatic(ConnectorTransferHandler.class)
        when(ConnectorTransferHandler.getOneOfTransferCtx(anyLong())).thenReturn(ctx1)

        def ctx = Mock(ChannelHandlerContext) {
            channel() >> Mock(Channel) {
                attr(Conn.NET_ID) >> Mock(Attribute)
            }
        }

        Chat.ChatMsg chat = Chat.ChatMsg.newBuilder()
                .setVersion(MsgVersion.V1.getVersion())
                .setId(1)
                .setCreateTime(System.currentTimeMillis())
                .setFromId("123")
                .setDestId("456")
                .setMsgType(Chat.ChatMsg.MsgType.TEXT)
                .setMsgBody(ByteString.copyFromUtf8("encodedMsg"))
                .setDestType(Chat.ChatMsg.DestType.SINGLE)
                .build()

        when:
        userOnlineService.userOffline(ctx)
        ch.writeInbound(chat)
        Thread.sleep(100)

        then:
        0 * ctx.writeAndFlush(_ as Chat.ChatMsg)
        1 * ctx1.writeAndFlush(chat)
    }

    def "test force offline"() {
        given:
        def handler = new ConnectorClientHandler(new ConnectorToClientService(clientConnContext),
                userOnlineService, clientConnContext)
        handler.setClientAckWindow(new ClientAckWindow(5))

        def ch = new EmbeddedChannel()
        ch.pipeline()
                .addLast("MsgDecoder", ConnectorStarter.injector.getInstance(MsgDecoder.class))
                .addLast("MsgEncoder", ConnectorStarter.injector.getInstance(MsgEncoder.class))
                .addLast("ConnectorClientHandler", handler)

        def connectorTransferCtx = Mock(ChannelHandlerContext)
        PowerMockito.mockStatic(ConnectorTransferHandler.class)
        when(ConnectorTransferHandler.getCtxList()).thenReturn(Lists.newArrayList(connectorTransferCtx))

        def map = new HashMap<String, Object>()
        def ctx = Mock(ChannelHandlerContext) {
            channel() >> Mock(Channel) {
                attr(Conn.NET_ID) >> Mock(Attribute) {
                    set() >> { arguments -> map.put("net_id", arguments[0]) }
                    get() >> { map.get("net_id") }
                }
            }
        }
        //user online
        userOnlineService.userOnline("123", ctx)

        when:
        //user online again
        Internal.InternalMsg greet = Internal.InternalMsg.newBuilder()
                .setVersion(MsgVersion.V1.getVersion())
                .setId(111112)
                .setCreateTime(System.currentTimeMillis())
                .setFrom(Internal.InternalMsg.Module.CLIENT)
                .setDest(Internal.InternalMsg.Module.CONNECTOR)
                .setMsgType(Internal.InternalMsg.MsgType.GREET)
                .setMsgBody("123")
                .build()
        ch.writeInbound(greet)

        then:
        clientConnContext.getConn(ctx) == null
        clientConnContext.getConnByUserId("123") != null
    }
}
