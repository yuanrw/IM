package com.github.yuanrw.im.client;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.github.yuanrw.im.client.api.ChatApi;
import com.github.yuanrw.im.client.api.ClientMsgListener;
import com.github.yuanrw.im.client.api.UserApi;
import com.github.yuanrw.im.client.context.UserContext;
import com.github.yuanrw.im.client.handler.ClientConnectorHandler;
import com.github.yuanrw.im.client.handler.code.AesDecoder;
import com.github.yuanrw.im.client.handler.code.AesEncoder;
import com.github.yuanrw.im.common.code.MsgDecoder;
import com.github.yuanrw.im.common.code.MsgEncoder;
import com.github.yuanrw.im.common.exception.ImException;
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
 * client's connection info
 * Date: 2019-04-15
 * Time: 16:42
 *
 * @author yrw
 */
public class ImClient {
    private static Logger logger = LoggerFactory.getLogger(ImClient.class);

    public Injector injector;

    private String connectorHost;
    private Integer connectorPort;
    private ClientMsgListener clientMsgListener;

    public ImClient(String connectorHost, Integer connectorPort, String restUrl) {
        this(connectorHost, connectorPort, restUrl, null);
    }

    public ImClient(String connectorHost, Integer connectorPort, String restUrl, ClientMsgListener clientMsgListener) {
        assert connectorHost != null;
        assert connectorPort != null;
        assert restUrl != null;

        this.connectorHost = connectorHost;
        this.connectorPort = connectorPort;
        this.clientMsgListener = clientMsgListener;

        ClientRestServiceProvider.REST_URL = restUrl;
        this.injector = Guice.createInjector(new ClientModule());
    }

    public void start() {
        assert clientMsgListener != null;

        UserContext userContext = injector.getInstance(UserContext.class);
        ClientConnectorHandler handler = new ClientConnectorHandler(clientMsgListener);
        userContext.setClientConnectorHandler(handler);

        startImClient(handler);
    }

    private void startImClient(ClientConnectorHandler handler) {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        ChannelFuture f = b.group(group)
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    ChannelPipeline p = ch.pipeline();

                    //out
                    p.addLast("MsgEncoder", injector.getInstance(MsgEncoder.class));
                    p.addLast("AesEncoder", injector.getInstance(AesEncoder.class));

                    //in
                    p.addLast("MsgDecoder", injector.getInstance(MsgDecoder.class));
                    p.addLast("AesDecoder", injector.getInstance(AesDecoder.class));
                    p.addLast("ClientConnectorHandler", handler);
                }
            }).connect(connectorHost, connectorPort)
            .addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    logger.info("ImClient connect to connector successfully");
                } else {
                    throw new ImException("[client] connect to connector failed!");
                }
            });

        try {
            f.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new ImException("[client] connect to connector failed!");
        }
    }

    public String getConnectorHost() {
        return connectorHost;
    }

    public Integer getConnectorPort() {
        return connectorPort;
    }

    public ClientMsgListener getClientMsgListener() {
        return clientMsgListener;
    }

    public void setClientMsgListener(ClientMsgListener clientMsgListener) {
        this.clientMsgListener = clientMsgListener;
    }

    public <T> T getApi(Class<T> clazz) {
        assert clazz == UserApi.class || clazz == ChatApi.class;
        return injector.getInstance(clazz);
    }
}
