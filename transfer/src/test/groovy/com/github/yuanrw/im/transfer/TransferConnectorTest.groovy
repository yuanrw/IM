package com.github.yuanrw.im.transfer

import com.github.yuanrw.im.common.code.MsgDecoder
import com.github.yuanrw.im.common.code.MsgEncoder
import com.github.yuanrw.im.common.domain.conn.Conn
import com.github.yuanrw.im.common.domain.conn.ConnectorConn
import com.github.yuanrw.im.common.domain.constant.MsgVersion
import com.github.yuanrw.im.common.util.IdWorker
import com.github.yuanrw.im.protobuf.generate.Ack
import com.github.yuanrw.im.protobuf.generate.Chat
import com.github.yuanrw.im.protobuf.generate.Internal
import com.github.yuanrw.im.transfer.domain.ConnectorConnContext
import com.github.yuanrw.im.transfer.handler.TransferConnectorHandler
import com.github.yuanrw.im.transfer.service.TransferService
import com.github.yuanrw.im.transfer.start.TransferStarter
import com.github.yuanrw.im.user.status.factory.UserStatusServiceFactory
import com.github.yuanrw.im.user.status.service.impl.MemoryUserStatusServiceImpl
import com.google.protobuf.ByteString
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.util.Attribute
import spock.lang.Shared
import spock.lang.Specification

/**
 * Date: 2019-06-07
 * Time: 17:54
 * @author yrw
 */
class TransferConnectorTest extends Specification {

    @Shared
    def ch = new EmbeddedChannel()
    @Shared
    def connectorConnContext
    @Shared
    def userStatusService = new MemoryUserStatusServiceImpl()
    @Shared
    TransferService transferService

    def setupSpec() {
        TransferStarter.TRANSFER_CONFIG.setRedisHost("redisHost")
        TransferStarter.TRANSFER_CONFIG.setRedisPort(123)
        TransferStarter.TRANSFER_CONFIG.setRedisPassword("redisPassword")

        def userStatusServiceFactory = Mock(UserStatusServiceFactory) {
            createService(_ as Properties) >> userStatusService
        }
        connectorConnContext = new ConnectorConnContext(userStatusServiceFactory)
        transferService = new TransferService(connectorConnContext)

        ch.pipeline()
                .addLast("MsgDecoder", new MsgDecoder())
                .addLast("MsgEncoder", new MsgEncoder())
                .addLast("TransferConnectorHandler", new TransferConnectorHandler(transferService, connectorConnContext))
    }

    def "test get internal greet"() {
        when:
        Internal.InternalMsg greet = Internal.InternalMsg.newBuilder()
                .setVersion(MsgVersion.V1.getVersion())
                .setId(IdWorker.genId())
                .setCreateTime(System.currentTimeMillis())
                .setFrom(Internal.InternalMsg.Module.CONNECTOR)
                .setDest(Internal.InternalMsg.Module.TRANSFER)
                .setMsgType(Internal.InternalMsg.MsgType.GREET)
                .setMsgBody("123aad2936sko59")
                .build()
        ch.writeInbound(greet)

        then:
        connectorConnContext.getConn("123aad2936sko59") != null
    }


    def "test get chat online"() {
        given:
        def ctx = Mock(ChannelHandlerContext) {
            channel() >> Mock(Channel) {
                attr(Conn.NET_ID) >> Mock(Attribute) {
                    get() >> "2837498274938"
                }
            }
        }
        def conn = new ConnectorConn(ctx)
        connectorConnContext.addConn(conn)
        userStatusService.online("5678", conn.getNetId().toString())

        Chat.ChatMsg chat = Chat.ChatMsg.newBuilder()
                .setVersion(MsgVersion.V1.getVersion())
                .setId(IdWorker.genId())
                .setCreateTime(System.currentTimeMillis())
                .setFromId("1234")
                .setDestId("5678")
                .setMsgType(Chat.ChatMsg.MsgType.TEXT)
                .setDestType(Chat.ChatMsg.DestType.SINGLE)
                .setMsgBody(ByteString.copyFromUtf8("123aad2936sko59"))
                .build()

        when:
        ch.writeInbound(chat)

        then:
        1 * ctx.writeAndFlush(chat)
    }

    def "test get ack online"() {
        given:
        def ctx = Mock(ChannelHandlerContext) {
            channel() >> Mock(Channel) {
                attr(Conn.NET_ID) >> Mock(Attribute) {
                    get() >> "2837498274938"
                }
            }
        }
        def conn = new ConnectorConn(ctx)
        connectorConnContext.addConn(conn)
        userStatusService.online("5678", conn.getNetId().toString())

        Ack.AckMsg ack = Ack.AckMsg.newBuilder()
                .setVersion(MsgVersion.V1.getVersion())
                .setId(IdWorker.genId())
                .setCreateTime(System.currentTimeMillis())
                .setFromId("1234")
                .setDestId("5678")
                .setMsgType(Ack.AckMsg.MsgType.DELIVERED)
                .setDestType(Ack.AckMsg.DestType.SINGLE)
                .setAckMsgId(12345)
                .build()

        when:
        ch.writeInbound(ack)

        then:
        1 * ctx.writeAndFlush(ack)
    }
}