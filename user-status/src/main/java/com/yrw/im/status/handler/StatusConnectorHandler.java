package com.yrw.im.status.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.protobuf.Message;
import com.yrw.im.common.domain.AbstractMessageParser;
import com.yrw.im.common.domain.UserStatus;
import com.yrw.im.common.domain.conn.Conn;
import com.yrw.im.common.function.ImBiConsumer;
import com.yrw.im.proto.constant.UserStatusEnum;
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
 * Time: 21:38
 *
 * @author yrw
 */
public class StatusConnectorHandler extends SimpleChannelInboundHandler<Message> {
    private static Logger logger = LoggerFactory.getLogger(StatusConnectorHandler.class);

    private ObjectMapper objectMapper;
    private UserStatusService userStatusService;
    private FromConnectorParser fromConnectorParser;

    @Inject
    public StatusConnectorHandler(UserStatusService userStatusService) {
        this.objectMapper = new ObjectMapper();
        this.userStatusService = userStatusService;
        this.fromConnectorParser = new FromConnectorParser();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        logger.debug("[status] get msg: {}", msg.toString());

        checkFrom(msg, Internal.InternalMsg.Module.CONNECTOR);
        checkDest(msg, Internal.InternalMsg.Module.STATUS);

        fromConnectorParser.parse(msg, ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        userStatusService.connectorDone(ctx.channel().attr(Conn.NET_ID).toString());
    }

    class FromConnectorParser extends AbstractMessageParser {

        @Override
        public void registerParsers() {
            ImBiConsumer<Internal.InternalMsg, ChannelHandlerContext> parse = (m, ctx) -> {
                checkMsgType(m, Internal.InternalMsg.InternalMsgType.USER_STATUS);

                UserStatus userStatus = objectMapper.readValue(m.getMsgBody(), UserStatus.class);
                switch (UserStatusEnum.getStatus(userStatus.getStatus())) {
                    case ONLINE:
                        userStatusService.online(userStatus.getConnectorId(), userStatus.getUserId());

                        Internal.InternalMsg resp = Internal.InternalMsg.newBuilder()
                            .setVersion(1)
                            .setCreateTime(System.currentTimeMillis())
                            .setFrom(Internal.InternalMsg.Module.STATUS)
                            .setDest(Internal.InternalMsg.Module.CONNECTOR)
                            .setMsgType(Internal.InternalMsg.InternalMsgType.ACK)
                            .setMsgBody(m.getId() + "")
                            .build();

                        ctx.writeAndFlush(resp);
                        break;
                    case OFFLINE:
                        userStatusService.offline(userStatus.getConnectorId(), userStatus.getUserId());
                        break;
                    default:
                }
            };

            register(Internal.InternalMsg.class, parse);
        }
    }
}
