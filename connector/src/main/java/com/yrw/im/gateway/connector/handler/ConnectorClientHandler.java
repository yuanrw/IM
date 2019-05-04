package com.yrw.im.gateway.connector.handler;

import com.google.inject.Inject;
import com.google.protobuf.Message;
import com.yrw.im.gateway.connector.domain.ClientConnContext;
import com.yrw.im.gateway.connector.service.ClientMsgService;
import com.yrw.im.proto.generate.Chat;
import com.yrw.im.proto.generate.Internal;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 处理客户端的消息
 * Date: 2019-02-09
 * Time: 23:26
 *
 * @author yrw
 */
public class ConnectorClientHandler extends SimpleChannelInboundHandler<Message> {

    private Logger logger = LoggerFactory.getLogger(ConnectorClientHandler.class);

    private ClientMsgService clientMsgService;
    private ClientConnContext clientConnContext;

    @Inject
    public ConnectorClientHandler(ClientMsgService clientMsgService, ClientConnContext clientConnContext) {
        this.clientMsgService = clientMsgService;
        this.clientConnContext = clientConnContext;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        if (msg instanceof Chat.ChatMsg) {
            clientMsgService.doChat((Chat.ChatMsg) msg);
            return;
        } else if (msg instanceof Internal.InternalMsg) {
            Internal.InternalMsg internalMsg = (Internal.InternalMsg) msg;
            if (internalMsg.getMsgType() == Internal.InternalMsg.InternalMsgType.GREET) {
                clientMsgService.doGreet((Internal.InternalMsg) msg, ctx);
                return;
            }
        }
        logger.warn("[IM ConnectorClientHandler] unexpected msg: {}", msg.toString());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        clientConnContext.removeConn(ctx);
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("[IM ConnectorClientHandler] has error: ", cause);
        clientConnContext.removeConn(ctx);
        ctx.close();
    }
}
