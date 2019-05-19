package com.yrw.im.gateway.connector.service;

import com.google.inject.Inject;
import com.yrw.im.common.domain.conn.Conn;
import com.yrw.im.gateway.connector.domain.ClientConnContext;
import com.yrw.im.gateway.connector.handler.ConnectorTransferHandler;
import com.yrw.im.gateway.connector.start.ConnectorClient;
import com.yrw.im.proto.generate.Chat;

/**
 * 消息执行器
 * Date: 2019-04-08
 * Time: 21:05
 *
 * @author yrw
 */
public class ConnectorService {

    private ClientConnContext clientConnContext;

    @Inject
    public ConnectorService() {
        this.clientConnContext = ConnectorClient.injector.getInstance(ClientConnContext.class);
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
