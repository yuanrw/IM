package com.yrw.im.transfer;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.yrw.im.proto.code.MsgDecoder;
import com.yrw.im.proto.code.MsgEncoder;
import com.yrw.im.transfer.handler.TransferConnectorHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Date: 2019-04-12
 * Time: 18:16
 *
 * @author yrw
 */
public class TransferServer {
    private static Logger logger = LoggerFactory.getLogger(TransferServer.class);

    private static Injector injector = Guice.createInjector();

    public static void startTransferServer(int port) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap()
            .group(bossGroup, workGroup)
            .channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel channel) throws Exception {
                    ChannelPipeline pipeline = channel.pipeline();
                    pipeline.addLast("MsgDecoder", injector.getInstance(MsgDecoder.class));
                    pipeline.addLast("MsgEncoder", new MsgEncoder());
                    pipeline.addLast("TransferClientHandler", injector.getInstance(TransferConnectorHandler.class));
                }
            });

        bootstrap.bind(new InetSocketAddress(port)).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                //TODO: do some init
                logger.info("[IM transfer] start successful, waiting for connectors connecting......");
            } else {
                logger.error("[IM transfer] start failed!");
            }
        });
    }
}
