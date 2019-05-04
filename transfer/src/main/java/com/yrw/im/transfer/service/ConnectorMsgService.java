package com.yrw.im.transfer.service;

import com.google.inject.Inject;
import com.yrw.im.common.domain.conn.Conn;
import com.yrw.im.proto.generate.Chat;
import com.yrw.im.proto.generate.Internal;
import com.yrw.im.transfer.domain.ConnectorConn;
import com.yrw.im.transfer.domain.ConnectorConnContext;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Date: 2019-05-04
 * Time: 13:47
 *
 * @author yrw
 */
public class ConnectorMsgService {
    private static final Logger logger = LoggerFactory.getLogger(ConnectorMsgService.class);

    private ConnectorConnContext connContext;

    @Inject
    public ConnectorMsgService(ConnectorConnContext connContext) {
        this.connContext = connContext;
    }

    public void doChat(Chat.ChatMsg msg) {
        ConnectorConn conn = connContext.getConnByUserId(msg.getDestId());

        if (conn != null) {
            conn.getCtx().writeAndFlush(msg);
        } else {
            logger.info("offline, msg: {}", msg);
        }
    }

    public void doGreet(Internal.InternalMsg msg, ChannelHandlerContext ctx) {
        String netId = msg.getMsgBody();
        ctx.channel().attr(Conn.NET_ID).set(netId);

        ConnectorConn conn = new ConnectorConn(ctx);
        connContext.addConn(conn);
    }

    public void doUpdateUserStatus(Internal.InternalMsg msg, ChannelHandlerContext ctx) {
        Long userId = Long.parseLong(msg.getMsgBody());
        connContext.addUser(userId, ctx);
    }
}
