package com.github.yuanrw.im.transfer.start

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.yuanrw.im.common.code.MsgDecoder
import com.github.yuanrw.im.common.code.MsgEncoder
import com.github.yuanrw.im.common.domain.UserStatus
import com.github.yuanrw.im.common.util.IdWorker
import com.github.yuanrw.im.protobuf.constant.UserStatusEnum
import com.github.yuanrw.im.protobuf.generate.Internal
import com.github.yuanrw.im.transfer.config.TransferConfig
import com.github.yuanrw.im.transfer.domain.ConnectorConnContext
import com.github.yuanrw.im.transfer.handler.TransferConnectorHandler
import com.github.yuanrw.im.transfer.service.TransferService
import com.github.yuanrw.im.user.status.factory.UserStatusServiceFactory
import com.github.yuanrw.im.user.status.service.UserStatusService
import io.netty.channel.embedded.EmbeddedChannel
import spock.lang.Specification

/**
 * Date: 2019-06-07
 * Time: 17:54
 * @author yrw
 */
class TransferConnectorTest extends Specification {

    def setupSpec() {
        def config = new TransferConfig()
        config.setRedisHost("host")
        config.setRedisPort(123)
        TransferStarter.TRANSFER_CONFIG = config
    }

    def "test get internal greet"() {
        given:
        def userStatusService = Mock(UserStatusService) {
            online(_ as String, _ as String) >> null
        }
        def userStatusServiceFactory = Mock(UserStatusServiceFactory) {
            createService(_ as String, _ as Integer) >> userStatusService
        }

        def connectorConnContext = new ConnectorConnContext(userStatusServiceFactory)

        def channel = new EmbeddedChannel()
        channel.pipeline()
                .addLast("MsgDecoder", TransferServer.injector.getInstance(MsgDecoder.class))
                .addLast("MsgEncoder", TransferServer.injector.getInstance(MsgEncoder.class))
                .addLast("TransferConnectorHandler", new TransferConnectorHandler(new TransferService(connectorConnContext), connectorConnContext))

        when:
        Internal.InternalMsg greet = Internal.InternalMsg.newBuilder()
                .setVersion(1)
                .setId(IdWorker.genId())
                .setCreateTime(System.currentTimeMillis())
                .setFrom(Internal.InternalMsg.Module.CONNECTOR)
                .setDest(Internal.InternalMsg.Module.TRANSFER)
                .setMsgType(Internal.InternalMsg.MsgType.GREET)
                .setMsgBody("123aad2936sko59")
                .build()
        channel.writeInbound(greet)

        then:
        connectorConnContext.getConn("123aad2936sko59") != null
    }

    def "test get internal user status"() {
        given:
        def userStatusService = Mock(UserStatusService) {
            online(_ as String, _ as String) >> null
        }
        def userStatusServiceFactory = Mock(UserStatusServiceFactory) {
            createService(_ as String, _ as Integer) >> userStatusService
        }
        def connectorConnContext = new ConnectorConnContext(userStatusServiceFactory)

        def channel = new EmbeddedChannel()
        channel.pipeline()
                .addLast("MsgDecoder", TransferServer.injector.getInstance(MsgDecoder.class))
                .addLast("MsgEncoder", TransferServer.injector.getInstance(MsgEncoder.class))
                .addLast("TransferConnectorHandler", new TransferConnectorHandler(new TransferService(connectorConnContext), connectorConnContext))

        when:
        //greet first
        Internal.InternalMsg greet = Internal.InternalMsg.newBuilder()
                .setVersion(1)
                .setId(IdWorker.genId())
                .setCreateTime(System.currentTimeMillis())
                .setFrom(Internal.InternalMsg.Module.CONNECTOR)
                .setDest(Internal.InternalMsg.Module.TRANSFER)
                .setMsgType(Internal.InternalMsg.MsgType.GREET)
                .setMsgBody("123aad2936sko59")
                .build()
        channel.writeInbound(greet)

        def online = new UserStatus()
        online.setUserId("123")
        online.setStatus(UserStatusEnum.ONLINE.getCode())

        Internal.InternalMsg msg = Internal.InternalMsg.newBuilder()
                .setVersion(1)
                .setId(IdWorker.genId())
                .setCreateTime(System.currentTimeMillis())
                .setFrom(Internal.InternalMsg.Module.CONNECTOR)
                .setDest(Internal.InternalMsg.Module.TRANSFER)
                .setMsgType(Internal.InternalMsg.MsgType.USER_STATUS)
                .setMsgBody(new ObjectMapper().writeValueAsString(online))
                .build()
        channel.writeInbound(msg)

        then:
        1 * userStatusService.online(_ as String, "123")

        when:
        def offline = new UserStatus()
        offline.setUserId("123")
        offline.setStatus(UserStatusEnum.OFFLINE.getCode())

        msg = Internal.InternalMsg.newBuilder()
                .mergeFrom(msg)
                .setMsgBody(new ObjectMapper().writeValueAsString(offline))
                .build()
        channel.writeInbound(msg)

        then:
        userStatusService.offline("123")
    }

//    def "test force offline"() {
//        given:
//        def userStatusService = Mock(UserStatusService) {
//            online(_ as String, _ as Long) >> "12345753asdg"
//        }
//        def userStatusServiceFactory = Mock(UserStatusServiceFactory) {
//            createService(_ as String, _ as Integer) >> userStatusService
//        }
//        def connectorConnContext = new ConnectorConnContext(userStatusServiceFactory)
//
//        def ch = new EmbeddedChannel()
//        ch.pipeline().addLast("MsgDecoder", TransferServer.injector.getInstance(MsgDecoder.class))
//                .addLast("MsgEncoder", TransferServer.injector.getInstance(MsgEncoder.class))
//                .addLast("TransferConnectorHandler", new TransferConnectorHandler(new TransferService(connectorConnContext), connectorConnContext))
//
//        //fake a old connection
//        def ctx = Mock(ChannelHandlerContext) {
//            channel() >> Mock(Channel) {
//                attr(Conn.NET_ID) >> Mock(Attribute) {
//                    get() >> "12345753asdg"
//                }
//            }
//        }
//        ConnectorConn conn = new ConnectorConn(ctx)
//        connectorConnContext.addConn(conn)
//
//        Internal.InternalMsg forceOffline = Internal.InternalMsg.newBuilder()
//                .setVersion(1)
//                .setId(IdWorker.genId())
//                .setCreateTime(System.currentTimeMillis())
//                .setFrom(Internal.InternalMsg.Module.TRANSFER)
//                .setDest(Internal.InternalMsg.Module.CONNECTOR)
//                .setMsgType(Internal.InternalMsg.MsgType.FORCE_OFFLINE)
//                .setMsgBody(123 + "")
//                .build()
//
//        when:
//        //greet first
//        Internal.InternalMsg greet = Internal.InternalMsg.newBuilder()
//                .setVersion(1)
//                .setId(IdWorker.genId())
//                .setCreateTime(System.currentTimeMillis())
//                .setFrom(Internal.InternalMsg.Module.CONNECTOR)
//                .setDest(Internal.InternalMsg.Module.TRANSFER)
//                .setMsgType(Internal.InternalMsg.MsgType.GREET)
//                .setMsgBody("123aad2936sko59")
//                .build()
//        ch.writeOneInbound(greet)
//
//        //online twice
//        def o = new UserStatus()
//        o.setUserId(123L)
//        o.setStatus(UserStatusEnum.ONLINE.getCode())
//
//        Internal.InternalMsg msg = Internal.InternalMsg.newBuilder()
//                .setVersion(1)
//                .setId(IdWorker.genId())
//                .setCreateTime(System.currentTimeMillis())
//                .setFrom(Internal.InternalMsg.Module.CONNECTOR)
//                .setDest(Internal.InternalMsg.Module.TRANSFER)
//                .setMsgType(Internal.InternalMsg.MsgType.USER_STATUS)
//                .setMsgBody(new ObjectMapper().writeValueAsString(o))
//                .build()
//        ch.writeOneInbound(msg)
//
//        then:
//        _ * userStatusService.online(_ as String, 123)
//        1 * ctx.writeAndFlush(forceOffline)
//    }

    def "test get chat"() {

    }

    def "test get ack"() {

    }
}
