package com.yim.im.client.handler;

import com.google.protobuf.Message;
import com.yim.im.client.api.ClientMsgListener;
import com.yrw.im.common.domain.AbstractMessageParser;
import com.yrw.im.common.domain.ResponseCollector;
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
 * Date: 2019-04-15
 * Time: 16:42
 *
 * @author yrw
 */
public class ClientConnectorHandler extends SimpleChannelInboundHandler<Message> {
    private Logger logger = LoggerFactory.getLogger(ClientConnectorHandler.class);

    private static AtomicReference<ResponseCollector<Internal.InternalMsg>> initMsgCollector = new AtomicReference<>();
    private static ChannelHandlerContext ctx;
    private static ClientMsgListener clientMsgListener;

    private FromConnectorParser fromConnectorParser;

    public ClientConnectorHandler() {
        assert clientMsgListener != null;
        this.fromConnectorParser = new FromConnectorParser();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ClientConnectorHandler.ctx = ctx;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        logger.debug("[client] get msg: {}", msg.toString());

        checkFrom(msg, Internal.InternalMsg.Module.CONNECTOR);
        checkDest(msg, Internal.InternalMsg.Module.CLIENT);

        fromConnectorParser.parse(msg, ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("[client] has error: ", cause);
    }

    public static ChannelHandlerContext getCtx() {
        return ctx;
    }

    public ResponseCollector<Internal.InternalMsg> createInitCollector(Duration timeout) {
        ResponseCollector<Internal.InternalMsg> collector = new ResponseCollector<>(timeout);
        boolean success = initMsgCollector.compareAndSet(null, collector);
        if (!success) {
            ResponseCollector<Internal.InternalMsg> previousCollector = initMsgCollector.get();
            if (previousCollector == null) {
                return createInitCollector(timeout);
            }

            throw new IllegalStateException("Still waiting for init response from server");
        }
        return collector;
    }

    public static void setClientMsgListener(ClientMsgListener clientMsgListener) {
        ClientConnectorHandler.clientMsgListener = clientMsgListener;
    }

    class FromConnectorParser extends AbstractMessageParser {

        @Override
        public void registerParsers() {
            register(Chat.ChatMsg.class, (m, ctx) -> clientMsgListener.read(m));
            register(Internal.InternalMsg.class, (m, ctx) -> {
                if (m.getMsgType() == Internal.InternalMsg.InternalMsgType.ACK) {
                    connectorInitDone(m);
                } else {
                    logger.warn("[client] unexpected msg: {}", m.toString());
                }
            });
        }

        private void connectorInitDone(Internal.InternalMsg msg) {
            ResponseCollector<Internal.InternalMsg> collector = ClientConnectorHandler.initMsgCollector.get();
            if (collector != null) {
                ClientConnectorHandler.initMsgCollector.set(null);
                collector.getFuture().complete(msg);
            } else {
                logger.error("Unexpected response received: {}", msg);
            }
        }
    }
}
