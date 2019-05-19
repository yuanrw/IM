package com.yrw.im.gateway.connector.handler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.protobuf.Message;
import com.yrw.im.common.domain.AbstractMessageParser;
import com.yrw.im.common.domain.ResponseCollector;
import com.yrw.im.common.util.IdWorker;
import com.yrw.im.gateway.connector.service.ConnectorService;
import com.yrw.im.proto.generate.Chat;
import com.yrw.im.proto.generate.Internal;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static com.yrw.im.common.domain.AbstractMessageParser.checkDest;
import static com.yrw.im.common.domain.AbstractMessageParser.checkFrom;

/**
 * 将消息发送给transfer
 * Date: 2019-02-12
 * Time: 12:17
 *
 * @author yrw
 */
@Singleton
public class ConnectorTransferHandler extends SimpleChannelInboundHandler<Message> {
    private Logger logger = LoggerFactory.getLogger(ConnectorTransferHandler.class);

    private static ChannelHandlerContext ctx;

    private FromTransferParser fromTransferParser;
    private ConnectorService connectorService;
    private AtomicReference<ResponseCollector<Internal.InternalMsg>> userStatusMsgCollector = new AtomicReference<>();

    @Inject
    public ConnectorTransferHandler(ConnectorService connectorService) {
        this.fromTransferParser = new FromTransferParser();
        this.connectorService = connectorService;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("[ConnectorTransfer] connect success");
        ConnectorTransferHandler.ctx = ctx;

        Internal.InternalMsg greet = Internal.InternalMsg.newBuilder()
            .setId(IdWorker.genId())
            .setVersion(1)
            .setMsgType(Internal.InternalMsg.InternalMsgType.GREET)
            .setFrom(Internal.InternalMsg.Module.CONNECTOR)
            .setDest(Internal.InternalMsg.Module.TRANSFER)
            .setCreateTime(System.currentTimeMillis())
            .build();

        ctx.writeAndFlush(greet);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        logger.debug("[connector] get msg: {}", msg.toString());

        checkFrom(msg, Internal.InternalMsg.Module.TRANSFER);
        checkDest(msg, Internal.InternalMsg.Module.CONNECTOR);

        fromTransferParser.parse(msg, ctx);
    }

    public static ChannelHandlerContext getCtx() {
        return ctx;
    }

    public ResponseCollector<Internal.InternalMsg> createUserStatusMsgCollector(Duration timeout) {
        ResponseCollector<Internal.InternalMsg> collector = new ResponseCollector<>(timeout);
        boolean success = userStatusMsgCollector.compareAndSet(null, collector);
        if (!success) {
            ResponseCollector<Internal.InternalMsg> previousCollector = this.userStatusMsgCollector.get();
            if (previousCollector == null) {
                return createUserStatusMsgCollector(timeout);
            }

            throw new IllegalStateException("Still waiting for init response from server");
        }
        return collector;
    }


    class FromTransferParser extends AbstractMessageParser {

        @Override
        public void registerParsers() {
            register(Chat.ChatMsg.class, (m, ctx) -> connectorService.doChat((m)));
            register(Internal.InternalMsg.class, (m, ctx) -> {
                checkMsgType(m, Internal.InternalMsg.InternalMsgType.ACK);
                userStatusSyncDone(m);
            });
        }

        private void userStatusSyncDone(Internal.InternalMsg msg) {
            ResponseCollector<Internal.InternalMsg> collector = userStatusMsgCollector.get();
            if (collector != null) {
                userStatusMsgCollector.set(null);
                collector.getFuture().complete(msg);
            } else {
                logger.error("Unexpected response received: {}", msg);
            }
        }
    }
}
