package com.yrw.im.transfer.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.protobuf.Message;
import com.yrw.im.proto.generate.Chat;
import com.yrw.im.proto.generate.Internal;
import com.yrw.im.transfer.service.ConnectorMsgService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Date: 2019-04-12
 * Time: 18:17
 *
 * @author yrw
 */
public class TransferConnectorHandler extends SimpleChannelInboundHandler<Message> {
    private Logger logger = LoggerFactory.getLogger(TransferConnectorHandler.class);

    private ConnectorMsgService connectorMsgService;
    private static ChannelHandlerContext ctx;

    @Inject
    public TransferConnectorHandler(ConnectorMsgService connectorMsgService) {
        this.connectorMsgService = connectorMsgService;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        TransferConnectorHandler.ctx = ctx;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        logger.info("[Transfer] get msg: ");
        if (msg instanceof Chat.ChatMsg) {
            connectorMsgService.doChat((Chat.ChatMsg) msg);
        } else if (msg instanceof Internal.InternalMsg) {
            Internal.InternalMsg internalMsg = (Internal.InternalMsg) msg;
            switch (internalMsg.getMsgType()) {
                case GREET:
                    connectorMsgService.doGreet(ctx);
                    break;
                case USER_STATUS:
                    connectorMsgService.doUpdateUserStatus(internalMsg, ctx);
                    break;
                default:
                    logger.warn("[TransferConnectorHandler] unexpected msg: {}",
                        new ObjectMapper().writeValueAsString(msg));
            }
        }
    }

    public static ChannelHandlerContext getCtx() {
        return ctx;
    }
}
