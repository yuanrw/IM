package com.github.yuanrw.im.connector.handler;

import com.github.yuanrw.im.common.domain.ack.ClientAckWindow;
import com.github.yuanrw.im.common.domain.ack.ServerAckWindow;
import com.github.yuanrw.im.common.domain.constant.MsgVersion;
import com.github.yuanrw.im.common.exception.ImException;
import com.github.yuanrw.im.common.parse.AbstractMsgParser;
import com.github.yuanrw.im.common.parse.InternalParser;
import com.github.yuanrw.im.common.util.IdWorker;
import com.github.yuanrw.im.connector.domain.ClientConn;
import com.github.yuanrw.im.connector.domain.ClientConnContext;
import com.github.yuanrw.im.connector.service.ConnectorToClientService;
import com.github.yuanrw.im.connector.service.UserOnlineService;
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
import java.util.function.Consumer;

import static com.github.yuanrw.im.common.parse.AbstractMsgParser.checkDest;
import static com.github.yuanrw.im.common.parse.AbstractMsgParser.checkFrom;

/**
 * 处理客户端的消息
 * Date: 2019-02-09
 * Time: 23:26
 *
 * @author yrw
 */
public class ConnectorClientHandler extends SimpleChannelInboundHandler<Message> {
    private Logger logger = LoggerFactory.getLogger(ConnectorClientHandler.class);

    private ConnectorToClientService connectorToClientService;
    private UserOnlineService userOnlineService;
    private ClientConnContext clientConnContext;
    private FromClientParser fromClientParser;

    private ServerAckWindow serverAckWindow;
    private ClientAckWindow clientAckWindow;

    @Inject
    public ConnectorClientHandler(ConnectorToClientService connectorToClientService, UserOnlineService userOnlineService, ClientConnContext clientConnContext) {
        this.fromClientParser = new FromClientParser();
        this.connectorToClientService = connectorToClientService;
        this.userOnlineService = userOnlineService;
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
        //remove connection and update user's status
        userOnlineService.userOffline(ctx);
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

            //do not use clientAckWindow, buz don't know netId yet
            parser.register(Internal.InternalMsg.MsgType.GREET, (m, ctx) -> {
                ClientConn conn = userOnlineService.userOnline(m.getMsgBody(), ctx);
                serverAckWindow = new ServerAckWindow(conn.getNetId(), 10, Duration.ofSeconds(5));
                clientAckWindow = new ClientAckWindow(5);
                ctx.writeAndFlush(getAck(m.getId()));
            });

            parser.register(Internal.InternalMsg.MsgType.ACK, (m, ctx) ->
                serverAckWindow.ack(m));

            //now we know netId
            register(Chat.ChatMsg.class, (m, ctx) -> offerChat(m.getId(), m, ctx, ignore ->
                connectorToClientService.doChatToClientOrTransferAndFlush(m)));

            register(Ack.AckMsg.class, (m, ctx) -> offerAck(m.getId(), m, ctx, ignore ->
                connectorToClientService.doSendAckToClientOrTransferAndFlush(m))
            );
            register(Internal.InternalMsg.class, parser.generateFun());
        }

        private void offerChat(Long id, Chat.ChatMsg m, ChannelHandlerContext ctx, Consumer<Message> consumer) {
            Chat.ChatMsg copy = Chat.ChatMsg.newBuilder().mergeFrom(m).build();
            offer(id, copy, ctx, consumer);
        }

        private void offerAck(Long id, Ack.AckMsg m, ChannelHandlerContext ctx, Consumer<Message> consumer) {
            Ack.AckMsg copy = Ack.AckMsg.newBuilder().mergeFrom(m).build();
            offer(id, copy, ctx, consumer);
        }

        private void offer(Long id, Message m, ChannelHandlerContext ctx, Consumer<Message> consumer) {
            if (clientAckWindow == null) {
                throw new ImException("client not greet yet");
            }
            clientAckWindow.offer(id,
                Internal.InternalMsg.Module.CONNECTOR,
                Internal.InternalMsg.Module.CLIENT,
                ctx, m, consumer);
        }
    }

    public Internal.InternalMsg getAck(Long id) {
        return Internal.InternalMsg.newBuilder()
            .setVersion(MsgVersion.V1.getVersion())
            .setId(IdWorker.snowGenId())
            .setFrom(Internal.InternalMsg.Module.CONNECTOR)
            .setDest(Internal.InternalMsg.Module.CLIENT)
            .setCreateTime(System.currentTimeMillis())
            .setMsgType(Internal.InternalMsg.MsgType.ACK)
            .setMsgBody(id + "")
            .build();
    }

    public void setClientAckWindow(ClientAckWindow clientAckWindow) {
        this.clientAckWindow = clientAckWindow;
    }
}
