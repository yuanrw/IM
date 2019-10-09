package com.github.yuanrw.im.connector.handler;

import com.github.yuanrw.im.common.domain.ack.ServerAckWindow;
import com.github.yuanrw.im.common.domain.conn.Conn;
import com.github.yuanrw.im.common.domain.constant.MsgVersion;
import com.github.yuanrw.im.common.parse.AbstractMsgParser;
import com.github.yuanrw.im.common.parse.InternalParser;
import com.github.yuanrw.im.common.util.IdWorker;
import com.github.yuanrw.im.connector.service.ConnectorToClientService;
import com.github.yuanrw.im.protobuf.generate.Ack;
import com.github.yuanrw.im.protobuf.generate.Chat;
import com.github.yuanrw.im.protobuf.generate.Internal;
import com.google.inject.Inject;
import com.google.protobuf.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.github.yuanrw.im.common.parse.AbstractMsgParser.checkDest;
import static com.github.yuanrw.im.common.parse.AbstractMsgParser.checkFrom;

/**
 * send msg to transfer
 * stateless, shareable
 * Date: 2019-02-12
 * Time: 21:17
 *
 * @author yrw
 */
public class ConnectorTransferHandler extends SimpleChannelInboundHandler<Message> {
    public static final String CONNECTOR_ID = IdWorker.uuid();
    private static Logger logger = LoggerFactory.getLogger(ConnectorTransferHandler.class);
    private static List<ChannelHandlerContext> ctxList = new ArrayList<>();

    private ServerAckWindow serverAckWindow;
    private FromTransferParser fromTransferParser;
    private ConnectorToClientService connectorToClientService;

    @Inject
    public ConnectorTransferHandler(ConnectorToClientService connectorToClientService) {
        this.fromTransferParser = new FromTransferParser();
        this.connectorToClientService = connectorToClientService;
    }

    public static ChannelHandlerContext getOneOfTransferCtx(long time) {
        if (ctxList.size() == 0) {
            logger.warn("connector is not connected to a transfer!");
        }
        return ctxList.get((int) (time % ctxList.size()));
    }

    public static List<ChannelHandlerContext> getCtxList() {
        if (ctxList.size() == 0) {
            logger.warn("connector is not connected to a transfer!");
        }
        return ctxList;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("[ConnectorTransfer] connect to transfer");

        putConnectionId(ctx);
        serverAckWindow = new ServerAckWindow(IdWorker.uuid(), 10, Duration.ofSeconds(5));
        greetToTransfer(ctx);

        ctxList.add(ctx);
    }

    private void greetToTransfer(ChannelHandlerContext ctx) {
        Internal.InternalMsg greet = Internal.InternalMsg.newBuilder()
            .setId(IdWorker.snowGenId())
            .setVersion(MsgVersion.V1.getVersion())
            .setMsgType(Internal.InternalMsg.MsgType.GREET)
            .setMsgBody(CONNECTOR_ID)
            .setFrom(Internal.InternalMsg.Module.CONNECTOR)
            .setDest(Internal.InternalMsg.Module.TRANSFER)
            .setCreateTime(System.currentTimeMillis())
            .build();

        serverAckWindow.offer(greet.getId(), greet, ctx::writeAndFlush)
            .thenAccept(m -> logger.info("[connector] connect to transfer successfully"))
            .exceptionally(e -> {
                logger.error("[connector] waiting for transfer's response failed", e);
                return null;
            });
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        logger.debug("[connector] get msg: {}", msg.toString());

        checkFrom(msg, Internal.InternalMsg.Module.TRANSFER);
        checkDest(msg, Internal.InternalMsg.Module.CONNECTOR);

        fromTransferParser.parse(msg, ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //todo: reconnect
    }

    class FromTransferParser extends AbstractMsgParser {

        @Override
        public void registerParsers() {
            InternalParser parser = new InternalParser(3);
            parser.register(Internal.InternalMsg.MsgType.ACK, (m, ctx) -> serverAckWindow.ack(m));

            register(Chat.ChatMsg.class, (m, ctx) -> connectorToClientService.doChatToClientAndFlush(m));
            register(Ack.AckMsg.class, (m, ctx) -> connectorToClientService.doSendAckToClientAndFlush(m));
            register(Internal.InternalMsg.class, parser.generateFun());
        }
    }

    public void putConnectionId(ChannelHandlerContext ctx) {
        ctx.channel().attr(Conn.NET_ID).set(IdWorker.uuid());
    }
}
