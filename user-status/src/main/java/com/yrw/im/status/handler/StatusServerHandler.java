package com.yrw.im.status.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.protobuf.Message;
import com.yrw.im.common.domain.AbstractMessageParser;
import com.yrw.im.common.domain.UserStatus;
import com.yrw.im.common.domain.conn.Conn;
import com.yrw.im.common.domain.conn.InternalConn;
import com.yrw.im.common.domain.conn.MemoryConnContext;
import com.yrw.im.common.function.ImBiConsumer;
import com.yrw.im.common.util.IdWorker;
import com.yrw.im.proto.constant.UserStatusEnum;
import com.yrw.im.proto.generate.Internal;
import com.yrw.im.status.service.UserStatusService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.yrw.im.common.domain.AbstractMessageParser.checkDest;

/**
 * Date: 2019-05-19
 * Time: 21:38
 *
 * @author yrw
 */
public class StatusServerHandler extends SimpleChannelInboundHandler<Message> {
    private static Logger logger = LoggerFactory.getLogger(StatusServerHandler.class);

    private ObjectMapper objectMapper;
    private UserStatusService userStatusService;
    private MemoryConnContext<InternalConn> connContext;
    private FromConnectorTransferParser fromConnectorTransferParser;

    public static final String TRANSFER_ID = "transfer";
    public static final String CONNECTOR_ID = "connector";

    @Inject
    public StatusServerHandler(UserStatusService userStatusService, MemoryConnContext<InternalConn> connContext) {
        this.objectMapper = new ObjectMapper();
        this.userStatusService = userStatusService;
        this.connContext = connContext;
        this.fromConnectorTransferParser = new FromConnectorTransferParser();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        logger.debug("[status] get msg: {}", msg.toString());

        checkDest(msg, Internal.InternalMsg.Module.STATUS);

        fromConnectorTransferParser.parse(msg, ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        userStatusService.connectorDone(ctx.channel().attr(Conn.NET_ID).toString());
    }

    private class FromConnectorTransferParser extends AbstractMessageParser {

        @Override
        public void registerParsers() {
            ImBiConsumer<Internal.InternalMsg, ChannelHandlerContext> greetParser = (m, ctx) -> {
                ctx.channel().attr(Conn.NET_ID).set(m.getMsgBody());
                connContext.addConn(new InternalConn(ctx));
            };

            ImBiConsumer<Internal.InternalMsg, ChannelHandlerContext> fromTransfer = (m, ctx) -> {
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

            ImBiConsumer<Internal.InternalMsg, ChannelHandlerContext> fromConnector = (m, ctx) -> {
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

            ImBiConsumer<Internal.InternalMsg, ChannelHandlerContext> parser = (m, ctx) -> {
                if (m.getMsgType() == Internal.InternalMsg.InternalMsgType.GREET) {
                    greetParser.accept(m, ctx);
                } else {
                    switch (m.getFrom()) {
                        case TRANSFER:
                            fromTransfer.accept(m, ctx);
                            break;
                        case CONNECTOR:
                            fromConnector.accept(m, ctx);
                            break;
                        default:
                            logger.warn("[status] unexpected msg: {}", m.toString());
                    }
                }
            };
            register(Internal.InternalMsg.class, parser);
        }
    }
}
