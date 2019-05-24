package com.yim.im.client.handler;

import com.google.inject.Singleton;
import com.google.protobuf.Message;
import com.yim.im.client.api.ClientMsgListener;
import com.yrw.im.common.domain.AbstractMsgParser;
import com.yrw.im.common.domain.InternalMsgParser;
import com.yrw.im.common.domain.ResponseCollector;
import com.yrw.im.proto.generate.Chat;
import com.yrw.im.proto.generate.Internal;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static com.yrw.im.common.domain.AbstractMsgParser.checkDest;
import static com.yrw.im.common.domain.AbstractMsgParser.checkFrom;

/**
 * Date: 2019-04-15
 * Time: 16:42
 *
 * @author yrw
 */
@Singleton
public class ClientConnectorHandler extends SimpleChannelInboundHandler<Message> {
    private Logger logger = LoggerFactory.getLogger(ClientConnectorHandler.class);

    private static ChannelHandlerContext ctx;
    private static ClientMsgListener clientMsgListener;
    private static AtomicReference<ResponseCollector<Internal.InternalMsg>> respCollector = new AtomicReference<>();

    private FromConnectorParser fromConnectorParser;

    public ClientConnectorHandler() {
        assert clientMsgListener != null;
        this.fromConnectorParser = new FromConnectorParser();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ClientConnectorHandler.ctx = ctx;
        clientMsgListener.active();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        logger.debug("[client] get msg: {}", msg.toString());

        checkFrom(msg, Internal.InternalMsg.Module.CONNECTOR);
        checkDest(msg, Internal.InternalMsg.Module.CLIENT);

        fromConnectorParser.parse(msg, ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        clientMsgListener.inactive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("[client] has error: ", cause);
    }

    public static ChannelHandlerContext getCtx() {
        return ctx;
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

    public static void setClientMsgListener(ClientMsgListener clientMsgListener) {
        ClientConnectorHandler.clientMsgListener = clientMsgListener;
    }

    class FromConnectorParser extends AbstractMsgParser {

        @Override
        public void registerParsers() {
            InternalMsgParser parser = new InternalMsgParser(3);
            parser.register(Internal.InternalMsg.InternalMsgType.ACK, (m, ctx) -> {
                ResponseCollector<Internal.InternalMsg> collector = respCollector.get();
                if (collector != null) {
                    respCollector.set(null);
                    collector.getFuture().complete(m);
                } else {
                    logger.error("Unexpected response received: {}", m);
                }
            });

            register(Chat.ChatMsg.class, (m, ctx) -> clientMsgListener.read(m));
            register(Internal.InternalMsg.class, parser.generateFun());
        }
    }
}
