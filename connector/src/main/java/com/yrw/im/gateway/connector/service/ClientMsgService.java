package com.yrw.im.gateway.connector.service;

import com.google.inject.Inject;
import com.yrw.im.common.domain.conn.Conn;
import com.yrw.im.gateway.connector.domain.ClientConn;
import com.yrw.im.gateway.connector.domain.ClientConnContext;
import com.yrw.im.gateway.connector.handler.ConnectorTransferHandler;
import com.yrw.im.proto.generate.Chat;
import com.yrw.im.proto.generate.Internal;
import io.netty.channel.ChannelHandlerContext;

/**
 * 消息执行器
 * Date: 2019-04-08
 * Time: 21:05
 *
 * @author yrw
 */
public class ClientMsgService {

    private ClientConnContext clientConnContext;

    @Inject
    public ClientMsgService(ClientConnContext clientConnContext) {
        this.clientConnContext = clientConnContext;
    }

    public void doGreet(Internal.InternalMsg msg, ChannelHandlerContext ctx) {
        ClientConn conn = new ClientConn(ctx);
        Long userId = Long.parseLong(msg.getMsgBody());
        conn.setUserId(userId);

        clientConnContext.addConn(conn);
    }

    public void doChat(Chat.ChatMsg msg) {
        Conn conn = clientConnContext.getConnByUserId(msg.getDestId());
        if (conn == null) {
            //不在当前机器上
            ConnectorTransferHandler.getCtx().writeAndFlush(msg);
        } else {
            //在当前机器上，转发给用户
            //不保存历史记录
            conn.getCtx().writeAndFlush(msg);
        }
    }
}
