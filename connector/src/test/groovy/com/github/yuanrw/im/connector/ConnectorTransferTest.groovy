package com.github.yuanrw.im.connector

import com.github.yuanrw.im.common.code.MsgDecoder
import com.github.yuanrw.im.common.code.MsgEncoder
import com.github.yuanrw.im.common.domain.ack.ServerAckWindow
import com.github.yuanrw.im.common.domain.conn.Conn
import com.github.yuanrw.im.common.domain.constant.MsgVersion
import com.github.yuanrw.im.common.domain.po.Offline
import com.github.yuanrw.im.common.parse.ParseService
import com.github.yuanrw.im.connector.config.ConnectorRestServiceFactory
import com.github.yuanrw.im.connector.domain.ClientConnContext
import com.github.yuanrw.im.connector.handler.ConnectorTransferHandler
import com.github.yuanrw.im.connector.service.ConnectorToClientService
import com.github.yuanrw.im.connector.service.OfflineService
import com.github.yuanrw.im.connector.service.UserOnlineService
import com.github.yuanrw.im.connector.service.rest.ConnectorRestService
import com.github.yuanrw.im.connector.start.ConnectorStarter
import com.github.yuanrw.im.protobuf.generate.Ack
import com.github.yuanrw.im.protobuf.generate.Chat
import com.github.yuanrw.im.user.status.factory.UserStatusServiceFactory
import com.github.yuanrw.im.user.status.service.impl.MemoryUserStatusServiceImpl
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

import static org.powermock.api.mockito.PowerMockito.when

/**
 * Date: 2019-06-06
 * Time: 22:39
 * @author yrw
 */
@PowerMockIgnore(["com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"])
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(Sputnik.class)
@PrepareForTest(ConnectorTransferHandler.class)
class ConnectorTransferTest extends Specification {

    @Shared
    def clientConnContext = ConnectorStarter.injector.getInstance(ClientConnContext.class)
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

    def "test get chat online"() {
        given:
        def handler = new ConnectorTransferHandler(new ConnectorToClientService(clientConnContext))

        def c = new EmbeddedChannel()
        c.pipeline()
                .addLast("MsgDecoder", new MsgDecoder())
                .addLast("MsgEncoder", new MsgEncoder())
                .addLast("ConnectorTransferHandler", handler)

        def connectorTransferCtx = Mock(ChannelHandlerContext) {
            channel() >> Mock(Channel) {
                attr(Conn.NET_ID) >> Mock(Attribute) {
                    get() >> "1"
                }
            }
        }
        handler.putConnectionId(connectorTransferCtx)

        PowerMockito.mockStatic(ConnectorTransferHandler.class)
        when(ConnectorTransferHandler.getOneOfTransferCtx(Mockito.anyLong())).thenReturn(connectorTransferCtx)

        handler.channelActive(connectorTransferCtx)

        def map = new HashMap<String, Object>()
        def ctx = Mock(ChannelHandlerContext) {
            channel() >> Mock(Channel) {
                attr(Conn.NET_ID) >> Mock(Attribute) {
                    set() >> { arguments -> map.put("net_id", arguments[0]) }
                    get() >> { map.get("net_id") }
                }
            }
        }
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
        c.writeInbound(chat)

        then:
        1 * ctx.writeAndFlush(chat)
        //send delivered to transfer
        1 * connectorTransferCtx.writeAndFlush(_ as Ack.AckMsg)
    }

    def "test get chat offline"() {
        given:
        def c = new EmbeddedChannel()
        c.pipeline()
                .addLast("MsgDecoder", new MsgDecoder())
                .addLast("MsgEncoder", new MsgEncoder())
                .addLast("ConnectorTransferHandler", new ConnectorTransferHandler(new ConnectorToClientService(clientConnContext)))

        def connectorTransferCtx = Mock(ChannelHandlerContext)
        PowerMockito.mockStatic(ConnectorTransferHandler.class)
        when(ConnectorTransferHandler.getCtxList()).thenReturn(Lists.newArrayList(connectorTransferCtx))

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
        c.writeInbound(chat)

        then:
        0 * ctx.writeAndFlush(chat)
        //todo:
    }

    def "test get ack online"() {
        given:
        def c = new EmbeddedChannel()
        c.pipeline()
                .addLast("MsgDecoder", new MsgDecoder())
                .addLast("MsgEncoder", new MsgEncoder())
                .addLast("ConnectorTransferHandler", new ConnectorTransferHandler(new ConnectorToClientService(clientConnContext)))

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
        c.writeInbound(delivered)

        then:
        1 * ctx.writeAndFlush(delivered)
    }

    def "test get ack offline"() {
        given:
        def c = new EmbeddedChannel()
        c.pipeline()
                .addLast("MsgDecoder", new MsgDecoder())
                .addLast("MsgEncoder", new MsgEncoder())
                .addLast("ConnectorTransferHandler", new ConnectorTransferHandler(new ConnectorToClientService(clientConnContext)))

        def connectorTransferCtx = Mock(ChannelHandlerContext)
        PowerMockito.mockStatic(ConnectorTransferHandler.class)
        when(ConnectorTransferHandler.getCtxList()).thenReturn(Lists.newArrayList(connectorTransferCtx))

        def ctx = Mock(ChannelHandlerContext) {
            channel() >> Mock(Channel) {
                attr(Conn.NET_ID) >> Mock(Attribute)
            }
        }

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
        c.writeInbound(delivered)

        then:
        0 * ctx.writeAndFlush(delivered)
        //todo:
    }
}
