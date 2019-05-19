package com.yrw.im.transfer.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.protobuf.Message;
import com.yrw.im.common.domain.AbstractMessageParser;
import com.yrw.im.common.domain.ResponseCollector;
import com.yrw.im.common.domain.UserStatus;
import com.yrw.im.common.function.ImBiConsumer;
import com.yrw.im.proto.constant.UserStatusEnum;
import com.yrw.im.proto.generate.Internal;
import com.yrw.im.transfer.domain.ConnectorConnContext;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static com.yrw.im.common.domain.AbstractMessageParser.checkDest;
import static com.yrw.im.common.domain.AbstractMessageParser.checkFrom;

/**
 * Date: 2019-05-19
 * Time: 23:02
 *
 * @author yrw
 */
public class TransferStatusHandler extends SimpleChannelInboundHandler<Message> {
    private static Logger logger = LoggerFactory.getLogger(TransferStatusHandler.class);

    private static AtomicReference<ResponseCollector<Internal.InternalMsg>> respCollector = new AtomicReference<>();
    private static ChannelHandlerContext ctx;

    private ObjectMapper objectMapper;
    private ConnectorConnContext connectorConnContext;
    private FromStatusParser fromStatusParser;

    @Inject
    public TransferStatusHandler(ConnectorConnContext connectorConnContext) {
        this.objectMapper = new ObjectMapper();
        this.connectorConnContext = connectorConnContext;
        this.fromStatusParser = new FromStatusParser();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        TransferStatusHandler.ctx = ctx;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        logger.debug("[status] get msg: {}", msg.toString());

        checkFrom(msg, Internal.InternalMsg.Module.STATUS);
        checkDest(msg, Internal.InternalMsg.Module.TRANSFER);

        fromStatusParser.parse(msg, ctx);
    }

    public static ResponseCollector<Internal.InternalMsg> createCollector(Duration timeout) {
        ResponseCollector<Internal.InternalMsg> collector = new ResponseCollector<>(timeout);
        boolean success = respCollector.compareAndSet(null, collector);
        if (!success) {
            ResponseCollector<Internal.InternalMsg> previousCollector = respCollector.get();
            if (previousCollector == null) {
                return createCollector(timeout);
            }

            throw new IllegalStateException("Still waiting for init response from server");
        }
        return collector;
    }

    public static ChannelHandlerContext getCtx() {
        return ctx;
    }

    class FromStatusParser extends AbstractMessageParser {

        @Override
        public void registerParsers() {
            ImBiConsumer<Internal.InternalMsg, ChannelHandlerContext> parser = (m, c) -> {
                if (m.getMsgType() == Internal.InternalMsg.InternalMsgType.USER_STATUS) {
                    UserStatus userStatus = objectMapper.readValue(m.getMsgBody(), UserStatus.class);
                    if (userStatus.getStatus() == UserStatusEnum.OFFLINE.getCode()) {
                        connectorConnContext.removeUser(userStatus.getUserId());
                    }
                } else if (m.getMsgType() == Internal.InternalMsg.InternalMsgType.RESP) {
                    getResp(m);
                } else {
                    logger.warn("[status]");
                }
            };

            register(Internal.InternalMsg.class, parser);
        }

        private void getResp(Internal.InternalMsg msg) {
            ResponseCollector<Internal.InternalMsg> collector = respCollector.get();
            if (collector != null) {
                respCollector.set(null);
                collector.getFuture().complete(msg);
            } else {
                logger.error("Unexpected response received: {}", msg);
            }
        }
    }
}
