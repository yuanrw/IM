package com.yrw.im.gateway.connector.handler;

import com.google.inject.Inject;
import com.google.protobuf.Message;
import com.yrw.im.common.domain.AbstractMessageParser;
import com.yrw.im.common.exception.ImException;
import com.yrw.im.gateway.connector.domain.ClientConnContext;
import com.yrw.im.gateway.connector.service.MsgService;
import com.yrw.im.gateway.connector.service.UserStatusService;
import com.yrw.im.gateway.connector.start.ConnectorClient;
import com.yrw.im.proto.generate.Chat;
import com.yrw.im.proto.generate.Internal;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;

import static com.yrw.im.common.domain.AbstractMessageParser.checkDest;
import static com.yrw.im.common.domain.AbstractMessageParser.checkFrom;

/**
 * 处理客户端的消息
 * Date: 2019-02-09
 * Time: 23:26
 *
 * @author yrw
 */
public class ConnectorClientHandler extends SimpleChannelInboundHandler<Message> {
    private Logger logger = LoggerFactory.getLogger(ConnectorClientHandler.class);

    private MsgService msgService;
    private UserStatusService userStatusService;
    private ClientConnContext clientConnContext;
    private FromClientParser fromClientParser;

    @Inject
    public ConnectorClientHandler(MsgService msgService, UserStatusService userStatusService) {
        this.fromClientParser = new FromClientParser();
        this.msgService = msgService;
        this.userStatusService = userStatusService;
        this.clientConnContext = ConnectorClient.injector.getInstance(ClientConnContext.class);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        logger.debug("[connector] get msg: {}", msg.toString());

        checkFrom(msg, Internal.InternalMsg.Module.CLIENT);
        checkDest(msg, Internal.InternalMsg.Module.CONNECTOR);

        fromClientParser.parse(msg, ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        userStatusService.userOffline(ctx);
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("[IM ConnectorClientHandler] has error: ", cause);
        clientConnContext.removeConn(ctx);
        ctx.close();
    }

    class FromClientParser extends AbstractMessageParser {

        @Override
        public void registerParsers() {
            BiConsumer<Internal.InternalMsg, ChannelHandlerContext> parseInternal = (m, ctx) -> {
                try {
                    if (m.getMsgType() == Internal.InternalMsg.InternalMsgType.GREET) {
                        userStatusService.userOnline(m, ctx);
                    } else {
                        logger.warn("[connector] unexpected msg: {}", m.toString());
                    }
                } catch (Exception e) {
                    throw new ImException("");
                }
            };

            register(Chat.ChatMsg.class, (m, ctx) -> msgService.doChat(m));
            register(Internal.InternalMsg.class, parseInternal);
        }
    }
}
