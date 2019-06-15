package com.yrw.im.connector.handler;

import com.google.inject.Inject;
import com.google.protobuf.Message;
import com.yrw.im.common.parse.AbstractMsgParser;
import com.yrw.im.common.parse.InternalParser;
import com.yrw.im.connector.domain.ClientConnContext;
import com.yrw.im.connector.service.ConnectorService;
import com.yrw.im.connector.service.UserStatusService;
import com.yrw.im.proto.generate.Ack;
import com.yrw.im.proto.generate.Chat;
import com.yrw.im.proto.generate.Internal;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.yrw.im.common.parse.AbstractMsgParser.checkDest;
import static com.yrw.im.common.parse.AbstractMsgParser.checkFrom;

/**
 * 处理客户端的消息
 * Date: 2019-02-09
 * Time: 23:26
 *
 * @author yrw
 */
public class ConnectorClientHandler extends SimpleChannelInboundHandler<Message> {
    private Logger logger = LoggerFactory.getLogger(ConnectorClientHandler.class);

    private ConnectorService connectorService;
    private UserStatusService userStatusService;
    private ClientConnContext clientConnContext;
    private FromClientParser fromClientParser;

    @Inject
    public ConnectorClientHandler(ConnectorService connectorService, UserStatusService userStatusService, ClientConnContext clientConnContext) {
        this.fromClientParser = new FromClientParser();
        this.connectorService = connectorService;
        this.userStatusService = userStatusService;
        this.clientConnContext = clientConnContext;
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
        //删除连接并更新用户状态
        userStatusService.userOffline(ctx);
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("[IM ConnectorClientHandler] has error: ", cause);
        clientConnContext.removeConn(ctx);
    }

    class FromClientParser extends AbstractMsgParser {

        @Override
        public void registerParsers() {
            InternalParser parser = new InternalParser(3);
            parser.register(Internal.InternalMsg.MsgType.GREET,
                (m, ctx) -> userStatusService.userOnline(m.getId(), Long.parseLong(m.getMsgBody()), ctx));

            register(Chat.ChatMsg.class, (m, ctx) -> connectorService.doChat(m));
            register(Ack.AckMsg.class, (m, ctx) -> connectorService.doSendAck(m));
            register(Internal.InternalMsg.class, parser.generateFun());
        }
    }
}
