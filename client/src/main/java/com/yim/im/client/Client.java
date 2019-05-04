package com.yim.im.client;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.yim.im.client.handler.ClientHandler;
import com.yrw.im.proto.code.MsgDecoder;
import com.yrw.im.proto.code.MsgEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Date: 2019-04-15
 * Time: 16:42
 *
 * @author yrw
 */
public class Client {
    private static Logger logger = LoggerFactory.getLogger(Client.class);

    private static Injector injector = Guice.createInjector();

    public static void client() {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group)
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    ChannelPipeline p = ch.pipeline();
                    p.addLast("MsgDecoder", injector.getInstance(MsgDecoder.class));
                    p.addLast("MsgEncoder", new MsgEncoder());
                    p.addLast("ClientHandler", injector.getInstance(ClientHandler.class));
                }
            }).connect("127.0.0.1", 9081)
            .addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    logger.info("Client connect connector successfully...");
                } else {
                    logger.error("Client connect connector failed!");
                }
            });
    }

    public static void main(String[] args) {
        Client.client();
    }
}
