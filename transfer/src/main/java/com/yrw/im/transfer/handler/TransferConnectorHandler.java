package com.yrw.im.transfer.handler;

import com.google.inject.Inject;
import com.google.protobuf.Message;
import com.yrw.im.common.domain.AbstractMessageParser;
import com.yrw.im.proto.generate.Chat;
import com.yrw.im.proto.generate.Internal;
import com.yrw.im.transfer.service.TransferService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private TransferService transferService;
    private FromConnectorParser fromConnectorParser;

    @Inject
    public TransferConnectorHandler(TransferService transferService) {
        this.fromConnectorParser = new FromConnectorParser();
        this.transferService = transferService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        logger.debug("[transfer] get msg: {}", msg.toString());

        checkFrom(msg, Internal.InternalMsg.Module.CONNECTOR);
        checkDest(msg, Internal.InternalMsg.Module.TRANSFER);

        fromConnectorParser.parse(msg, ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    class FromConnectorParser extends AbstractMessageParser {

        @Override
        public void registerParsers() {
            register(Chat.ChatMsg.class, (m, ctx) -> transferService.doChat(m));
            register(Internal.InternalMsg.class, (m, ctx) -> {
                checkMsgType(m, Internal.InternalMsg.InternalMsgType.GREET);
                transferService.doGreet(ctx);
            });
        }
    }
}
