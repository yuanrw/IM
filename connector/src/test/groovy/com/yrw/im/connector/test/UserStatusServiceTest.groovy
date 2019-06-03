package com.yrw.im.connector.test

import com.google.inject.Injector
import com.yrw.im.common.domain.ResponseCollector
import com.yrw.im.common.domain.conn.Conn
import com.yrw.im.common.util.IdWorker
import com.yrw.im.connector.domain.ClientConnContext
import com.yrw.im.connector.handler.ConnectorTransferHandler
import com.yrw.im.connector.service.OfflineService
import com.yrw.im.connector.service.UserStatusService
import com.yrw.im.connector.start.ConnectorClient
import com.yrw.im.connector.start.ConnectorStarter
import com.yrw.im.proto.generate.Internal
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.util.Attribute
import org.junit.runner.RunWith
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PowerMockIgnore
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import org.powermock.modules.junit4.PowerMockRunnerDelegate
import org.spockframework.runtime.Sputnik
import spock.lang.Specification

import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.function.BiConsumer

import static org.powermock.api.mockito.PowerMockito.when

/**
 * Date: 2019-06-02
 * Time: 21:08
 * @author yrw
 */
@PowerMockIgnore(["com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"])
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(Sputnik.class)
@PrepareForTest([ConnectorStarter.class, ConnectorTransferHandler.class])
class UserStatusServiceTest extends Specification {

    def "test user online and offline"() {
        given:
        def netId = -999
        //mock connector-client ctx
        def connectorClientCtx = Mock(ChannelHandlerContext) {
            channel() >> Mock(Channel) {
                attr(Conn.NET_ID) >> Mock(Attribute) {
                    set(_ as Long) >> { arguments -> netId = arguments[0] }
                }
            }
        }

        //mock connector-transfer ctx
        def connectorTransferCtx = Mock(ChannelHandlerContext)
        PowerMockito.mockStatic(ConnectorTransferHandler.class)
        when(ConnectorTransferHandler.getCtx()).thenReturn(connectorTransferCtx)

        //mock connector-transfer handler
        def connectorTransferHandler = Mock(ConnectorTransferHandler) {
            createUserStatusMsgCollector(_ as Duration) >> Mock(ResponseCollector) {
                getFuture() >> Mock(CompletableFuture) {
                    whenComplete(_ as BiConsumer) >> Mock(CompletableFuture) {
                        get() >> {}
                    }
                }
            }
        }

        //mock client conn text
        def clientConnContext = new ClientConnContext()
        def injector = Mock(Injector) {
            getInstance(ClientConnContext) >> clientConnContext
            getInstance(ConnectorTransferHandler) >> connectorTransferHandler
        }
        ConnectorClient.injector = injector

        def userStatusService = new UserStatusService(Mock(OfflineService))

        def userId = "123"

        //online
        when:
        Internal.InternalMsg msg = Internal.InternalMsg.newBuilder()
                .setVersion(1)
                .setId(IdWorker.genId())
                .setCreateTime(System.currentTimeMillis())
                .setFrom(Internal.InternalMsg.Module.CLIENT)
                .setDest(Internal.InternalMsg.Module.CONNECTOR)
                .setMsgType(Internal.InternalMsg.MsgType.GREET)
                .setMsgBody(userId)
                .build()

        userStatusService.userOnline(msg, connectorClientCtx)

        then:
        1 * connectorTransferCtx.writeAndFlush(_ as Internal.InternalMsg)

        clientConnContext.getConnByUserId(123) != null

        netId >= 0

        //offline
        when:
        def connectorClientCtx1 = Mock(ChannelHandlerContext) {
            channel() >> Mock(Channel) {
                attr(Conn.NET_ID) >> Mock(Attribute) {
                    get() >> netId
                }
            }
        }

        userStatusService.userOffline(connectorClientCtx1)

        then:
        1 * connectorTransferCtx.writeAndFlush(_ as Internal.InternalMsg)

        clientConnContext.getConnByUserId(123) == null
//        clientConnContext.getConn(connectorClientCtx1) == null
    }
}
