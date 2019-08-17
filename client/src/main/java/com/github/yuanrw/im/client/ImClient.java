package com.github.yuanrw.im.client;

import com.github.yuanrw.im.client.api.ChatApi;
import com.github.yuanrw.im.client.api.ClientMsgListener;
import com.github.yuanrw.im.client.api.UserApi;
import com.github.yuanrw.im.client.context.UserContext;
import com.github.yuanrw.im.client.handler.ClientConnectorHandler;
import com.github.yuanrw.im.client.handler.code.AesDecoder;
import com.github.yuanrw.im.client.handler.code.AesEncoder;
import com.github.yuanrw.im.client.service.ClientRestService;
import com.github.yuanrw.im.common.code.MsgDecoder;
import com.github.yuanrw.im.common.code.MsgEncoder;
import com.github.yuanrw.im.common.exception.ImException;
import com.google.inject.Guice;
import com.google.inject.Injector;
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
    private UserContext userContext;
    private ClientConnectorHandler handler;

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

        userContext = injector.getInstance(UserContext.class);
        handler = new ClientConnectorHandler(clientMsgListener);
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
                    p.addLast("MsgEncoder", new MsgEncoder());
                    p.addLast("AesEncoder", new AesEncoder(userContext));

                    //in
                    p.addLast("MsgDecoder", new MsgDecoder());
                    p.addLast("AesDecoder", new AesDecoder(userContext));
                    p.addLast("ClientConnectorHandler", handler);
                }
            }).connect(connectorHost, connectorPort)
            .addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    logger.info("ImClient connect to connector successfully");
                } else {
                    throw new ImException("[client] connect to connector failed! connector url: "
                        + connectorHost + ":" + connectorPort);
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

    public ChatApi chatApi() {
        return new ChatApi(userContext, handler);
    }

    public UserApi userApi() {
        return new UserApi(injector.getInstance(ClientRestService.class), userContext, handler);
    }
}