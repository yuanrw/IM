package com.yrw.im.transfer.handler;

import com.google.inject.Inject;
import com.google.protobuf.Message;
import com.yrw.im.common.domain.AbstractMessageParser;
import com.yrw.im.common.exception.ImException;
import com.yrw.im.proto.generate.Chat;
import com.yrw.im.proto.generate.Internal;
import com.yrw.im.transfer.service.ConnectorMsgService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;

import static com.yrw.im.common.domain.AbstractMessageParser.checkDest;
import static com.yrw.im.common.domain.AbstractMessageParser.checkFrom;

/**
 * Date: 2019-04-12
 * Time: 18:17
 *
 * @author yrw
 */
public class TransferConnectorHandler extends SimpleChannelInboundHandler<Message> {
    private Logger logger = LoggerFactory.getLogger(TransferConnectorHandler.class);

    private ConnectorMsgService connectorMsgService;
    private FromConnectorParser fromConnectorParser;

    @Inject
    public TransferConnectorHandler(ConnectorMsgService connectorMsgService) {
        this.fromConnectorParser = new FromConnectorParser();
        this.connectorMsgService = connectorMsgService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        logger.debug("[transfer] get msg: {}", msg.toString());

        checkFrom(msg, Internal.InternalMsg.Module.CONNECTOR);
        checkDest(msg, Internal.InternalMsg.Module.TRANSFER);

        fromConnectorParser.parse(msg, ctx);
    }

    class FromConnectorParser extends AbstractMessageParser {

        @Override
        public void registerParsers() {
            BiConsumer<Chat.ChatMsg, ChannelHandlerContext> chatParser = (m, ctx) -> {
                try {
                    connectorMsgService.doChat(m);
                } catch (Exception e) {
                    throw new ImException("");
                }
            };

            BiConsumer<Internal.InternalMsg, ChannelHandlerContext> internalParser = (m, ctx) -> {
                try {
                    switch (m.getMsgType()) {
                        case GREET:
                            connectorMsgService.doGreet(ctx);
                            break;
                        case USER_STATUS:
                            connectorMsgService.doUpdateUserStatus(m, ctx);
                            break;
                        default:
                            logger.warn("[transfer] unexpected msg: {}", m.toString());
                    }
                } catch (Exception e) {
                    throw new ImException("");
                }
            };

            register(Chat.ChatMsg.class, chatParser);
            register(Internal.InternalMsg.class, internalParser);
        }
    }
}
