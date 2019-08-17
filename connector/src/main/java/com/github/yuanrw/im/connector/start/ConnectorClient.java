package com.github.yuanrw.im.connector.start;

import com.github.yuanrw.im.common.code.MsgDecoder;
import com.github.yuanrw.im.common.code.MsgEncoder;
import com.github.yuanrw.im.common.exception.ImException;
import com.github.yuanrw.im.connector.handler.ConnectorTransferHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Date: 2019-05-02
 * Time: 17:50
 *
 * @author yrw
 */
public class ConnectorClient {

    private static Logger logger = LoggerFactory.getLogger(ConnectorClient.class);

    static void start(String[] transferUrls) {
        for (String transferUrl : transferUrls) {
            String[] url = transferUrl.split(":");

            EventLoopGroup group = new NioEventLoopGroup();
            Bootstrap b = new Bootstrap();
            ChannelFuture f = b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast("MsgDecoder", ConnectorStarter.injector.getInstance(MsgDecoder.class));
                        p.addLast("MsgEncoder", ConnectorStarter.injector.getInstance(MsgEncoder.class));
                        p.addLast("ConnectorTransferHandler", ConnectorStarter.injector.getInstance(ConnectorTransferHandler.class));
                    }
                }).connect(url[0], Integer.parseInt(url[1]))
                .addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        logger.info("[connector] connect to transfer successfully");
                    } else {
                        throw new ImException("[connector] connect to transfer failed! transfer url: " + transferUrl);
                    }
                });

            try {
                f.get(10, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new ImException("[connector] connect to transfer failed! transfer url: " + transferUrl, e);
            }
        }
    }
}