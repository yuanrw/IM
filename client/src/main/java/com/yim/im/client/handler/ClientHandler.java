package com.yim.im.client.handler;

import com.google.protobuf.Message;
import com.yrw.im.common.exception.ImException;
import com.yrw.im.proto.generate.Chat;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Date: 2019-04-15
 * Time: 16:42
 *
 * @author yrw
 */
public class ClientHandler extends SimpleChannelInboundHandler<Message> {
    private Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    private static ChannelHandlerContext ctx;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ClientHandler.ctx = ctx;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        Chat.ChatMsg chat = (Chat.ChatMsg) msg;
        logger.info("[client] receive msg: {}", chat.getMsgBody().toString(CharsetUtil.UTF_8));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("[client] has error: ", cause);
        throw new ImException("client has error");
    }

    public static ChannelHandlerContext getCtx() {
        return ctx;
    }
}
