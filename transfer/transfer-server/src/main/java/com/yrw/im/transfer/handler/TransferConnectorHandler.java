package com.yrw.im.transfer.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.protobuf.Message;
import com.yrw.im.common.domain.UserStatus;
import com.yrw.im.common.parse.AbstractMsgParser;
import com.yrw.im.common.parse.InternalParser;
import com.yrw.im.common.util.IdWorker;
import com.yrw.im.proto.constant.UserStatusEnum;
import com.yrw.im.proto.generate.Ack;
import com.yrw.im.proto.generate.Chat;
import com.yrw.im.proto.generate.Internal;
import com.yrw.im.status.domain.ConnectorConnContext;
import com.yrw.im.transfer.service.TransferService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.yrw.im.common.parse.AbstractMsgParser.checkDest;
import static com.yrw.im.common.parse.AbstractMsgParser.checkFrom;

/**
 * Date: 2019-04-12
 * Time: 18:17
 *
 * @author yrw
 */
public class TransferConnectorHandler extends SimpleChannelInboundHandler<Message> {
    private Logger logger = LoggerFactory.getLogger(TransferConnectorHandler.class);

    private TransferService transferService;
    private ConnectorConnContext connectorConnContext;
    private FromConnectorParser fromConnectorParser;
    private ObjectMapper objectMapper;

    @Inject
    public TransferConnectorHandler(TransferService transferService, ConnectorConnContext connectorConnContext) {
        this.fromConnectorParser = new FromConnectorParser();
        this.objectMapper = new ObjectMapper();
        this.transferService = transferService;
        this.connectorConnContext = connectorConnContext;
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
        //删除连接并更新用户状态
        connectorConnContext.removeConn(ctx);
    }

    class FromConnectorParser extends AbstractMsgParser {

        @Override
        public void registerParsers() {
            InternalParser parser = new InternalParser(3);
            parser.register(Internal.InternalMsg.MsgType.GREET, (m, ctx) -> transferService.doGreet(m, ctx));
            parser.register(Internal.InternalMsg.MsgType.USER_STATUS, (m, ctx) -> {
                UserStatus userStatus = objectMapper.readValue(m.getMsgBody(), UserStatus.class);
                if (UserStatusEnum.getStatus(userStatus.getStatus()) == UserStatusEnum.ONLINE) {
                    connectorConnContext.online(ctx, userStatus.getUserId());
                    sendAckToConnector(m.getId(), ctx);
                } else {
                    connectorConnContext.offline(userStatus.getUserId());
                }
            });

            register(Chat.ChatMsg.class, (m, ctx) -> transferService.doChat(m));
            register(Ack.AckMsg.class, (m, ctx) -> transferService.doSendAck(m));
            register(Internal.InternalMsg.class, parser.generateFun());
        }

        private void sendAckToConnector(Long id, ChannelHandlerContext ctx) {
            Internal.InternalMsg msg = Internal.InternalMsg.newBuilder()
                .setVersion(1)
                .setId(IdWorker.genId())
                .setFrom(Internal.InternalMsg.Module.TRANSFER)
                .setDest(Internal.InternalMsg.Module.CONNECTOR)
                .setCreateTime(System.currentTimeMillis())
                .setMsgType(Internal.InternalMsg.MsgType.ACK)
                .setMsgBody(id + "")
                .build();
            ctx.writeAndFlush(msg);
        }
    }
}
