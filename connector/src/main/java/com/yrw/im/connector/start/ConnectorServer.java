package com.yrw.im.connector.start;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.yrw.im.common.code.MsgDecoder;
import com.yrw.im.common.code.MsgEncoder;
import com.yrw.im.common.exception.ImException;
import com.yrw.im.connector.handler.ConnectorClientHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Date: 2019-02-09
 * Time: 23:27
 *
 * @author yrw
 */
public class ConnectorServer {
    private static final Logger logger = LoggerFactory.getLogger(ConnectorServer.class);

    private static Injector injector = Guice.createInjector();

    static void start(int port) {
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
                    pipeline.addLast("MsgEncoder", injector.getInstance(MsgEncoder.class));
                    pipeline.addLast("ConnectorClientHandler", injector.getInstance(ConnectorClientHandler.class));
                }
            });

        ChannelFuture f = bootstrap.bind(new InetSocketAddress(port)).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                logger.info("[connector] start successfully, waiting for clients to connect...");
            } else {
                throw new ImException("[connector] start failed");
            }
        });

        try {
            f.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new ImException("[connector] start failed", e);
        }
    }
}
