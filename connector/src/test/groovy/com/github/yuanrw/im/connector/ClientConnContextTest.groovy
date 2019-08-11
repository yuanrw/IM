package com.github.yuanrw.im.connector

import com.github.yuanrw.im.common.domain.conn.Conn
import com.github.yuanrw.im.connector.domain.ClientConn
import com.github.yuanrw.im.connector.domain.ClientConnContext
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.util.Attribute
import spock.lang.Specification

/**
 * Date: 2019-06-01
 * Time: 17:39
 * @author yrw
 */
class ClientConnContextTest extends Specification {

    def "test client conn"() {
        given:
        def attribute = Mock(Attribute)
        def c = Mock(Channel) {
            attr(Conn.NET_ID) >> attribute
        }
        def ctx = Mock(ChannelHandlerContext) {
            channel() >> c
        }

        when:
        ClientConn conn = new ClientConn(ctx)
        conn.setUserId("123")
        then:
        conn.getCtx() == ctx
        conn.getUserId() == "123"
        conn.getNetId() >= 0L

        1 * ctx.channel().attr(Conn.NET_ID).set(_ as Long)
    }

    def "test client conn context"() {
        given:
        def attribute = Mock(Attribute)
        def ctx = Mock(ChannelHandlerContext) {
            channel() >> Mock(Channel) {
                attr(Conn.NET_ID) >> attribute
            }
        }
        def context = new ClientConnContext()

        when:
        def userId = "7073059"
        def conn = new ClientConn(ctx)
        conn.setUserId(userId)
        context.addConn(conn)

        ctx.channel().attr(Conn.NET_ID).get() >> conn.getNetId()

        then:
        context.getConn(ctx) == conn
        context.getConn(conn.getNetId()) == conn
        context.getConnByUserId(userId) == conn

        context.getConn("sndigso") == null
        context.getConnByUserId("2222") == null

        when:
        context.removeConn(conn.getNetId())
        context.removeConn("123135135")

        then:
        context.getConn(conn.getNetId()) == null
    }
}
