package com.yrw.im.status.handler;

import com.google.inject.Inject;
import com.google.protobuf.Message;
import com.yrw.im.common.domain.AbstractMessageParser;
import com.yrw.im.common.function.ImBiConsumer;
import com.yrw.im.common.util.IdWorker;
import com.yrw.im.proto.generate.Internal;
import com.yrw.im.status.service.UserStatusService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.yrw.im.common.domain.AbstractMessageParser.checkDest;
import static com.yrw.im.common.domain.AbstractMessageParser.checkFrom;

/**
 * Date: 2019-05-19
 * Time: 22:37
 *
 * @author yrw
 */
public class StatusTransferHandler extends SimpleChannelInboundHandler<Message> {
    private Logger logger = LoggerFactory.getLogger(StatusTransferHandler.class);

    private UserStatusService userStatusService;
    private FromTransferParser fromTransferParser;

    @Inject
    public StatusTransferHandler(UserStatusService userStatusService) {
        this.userStatusService = userStatusService;
        this.fromTransferParser = new FromTransferParser();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        logger.debug("[status] get msg: {}", msg.toString());

        checkFrom(msg, Internal.InternalMsg.Module.TRANSFER);
        checkDest(msg, Internal.InternalMsg.Module.STATUS);

        fromTransferParser.parse(msg, ctx);
    }

    class FromTransferParser extends AbstractMessageParser {

        @Override
        public void registerParsers() {
            ImBiConsumer<Internal.InternalMsg, ChannelHandlerContext> parser = (m, ctx) -> {
                checkMsgType(m, Internal.InternalMsg.InternalMsgType.REQ);
                String connectorId = userStatusService.getConnector(Long.parseLong(m.getMsgBody()));

                Internal.InternalMsg msg = Internal.InternalMsg.newBuilder()
                    .setId(IdWorker.genId())
                    .setVersion(1)
                    .setCreateTime(System.currentTimeMillis())
                    .setFrom(Internal.InternalMsg.Module.STATUS)
                    .setDest(Internal.InternalMsg.Module.TRANSFER)
                    .setMsgType(Internal.InternalMsg.InternalMsgType.RESP)
                    .setMsgBody(connectorId)
                    .build();

                ctx.writeAndFlush(msg);
            };

            register(Internal.InternalMsg.class, parser);
        }
    }
}
