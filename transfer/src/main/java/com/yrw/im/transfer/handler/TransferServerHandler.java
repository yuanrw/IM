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
public class TransferServerHandler extends SimpleChannelInboundHandler<Message> {
    private Logger logger = LoggerFactory.getLogger(TransferServerHandler.class);

    private ConnectorMsgService connectorMsgService;

    @Inject
    public TransferServerHandler(ConnectorMsgService connectorMsgService) {
        this.connectorMsgService = connectorMsgService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        if (msg instanceof Chat.ChatMsg) {
            connectorMsgService.doChat((Chat.ChatMsg) msg);
        } else if (msg instanceof Internal.InternalMsg) {
            Internal.InternalMsg internalMsg = (Internal.InternalMsg) msg;
            switch (internalMsg.getMsgType()) {
                case GREET:
                    connectorMsgService.doGreet(internalMsg, ctx);
                    break;
                case USER_STATUS:
                    connectorMsgService.doUpdateUserStatus(internalMsg, ctx);
                    break;
                default:
                    logger.warn("[TransferServerHandler] unexpected msg: {}",
                        new ObjectMapper().writeValueAsString(msg));
            }
        }
    }
}
