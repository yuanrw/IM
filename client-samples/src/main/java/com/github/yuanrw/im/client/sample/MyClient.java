package com.github.yuanrw.im.client.sample;

import com.google.protobuf.ByteString;
import com.github.yuanrw.im.client.ImClient;
import com.github.yuanrw.im.client.api.ChatApi;
import com.github.yuanrw.im.client.api.ClientMsgListener;
import com.github.yuanrw.im.client.api.UserApi;
import com.github.yuanrw.im.client.domain.Friend;
import com.github.yuanrw.im.common.domain.UserInfo;
import com.github.yuanrw.im.common.util.IdWorker;
import com.github.yuanrw.im.protobuf.generate.Chat;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.CharsetUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Date: 2019-07-09
 * Time: 10:08
 *
 * @author yrw
 */
class MyClient {
    private static Logger logger = LoggerFactory.getLogger(MyClient.class);

    private static ConcurrentMap<Long, CompletableFuture<Long>> hasSentFutureMap = new ConcurrentHashMap<>();
    private static ConcurrentMap<Long, CompletableFuture<Long>> hasDeliveredFutureMap = new ConcurrentHashMap<>();
    private static ConcurrentMap<Long, CompletableFuture<Long>> hasReadFutureMap = new ConcurrentHashMap<>();

    private ChatApi chatApi;
    private String connectorHost;
    private Integer connectorPort;
    private String restUrl;

    private UserInfo userInfo;

    private List<Friend> friends;

    static AtomicInteger sendMsg = new AtomicInteger(0);
    static AtomicInteger readMsg = new AtomicInteger(0);
    static AtomicInteger hasSentAck = new AtomicInteger(0);
    static AtomicInteger hasDeliveredAck = new AtomicInteger(0);
    static AtomicInteger hasReadAck = new AtomicInteger(0);
    static AtomicInteger hasException = new AtomicInteger(0);

    MyClient(String connectorHost, Integer connectorPort, String restUrl, String username, String password) {
        this.connectorHost = connectorHost;
        this.connectorPort = connectorPort;
        this.restUrl = restUrl;

        ImClient imClient = start();
        chatApi = imClient.getApi(ChatApi.class);
        UserApi userApi = imClient.getApi(UserApi.class);

        //login and get a token
        userInfo = userApi.login(username, DigestUtils.sha256Hex(password.getBytes(CharsetUtil.UTF_8)));
        //get friends list
        friends = userApi.friends(userInfo.getToken());
    }

    private ImClient start() {
        ImClient imClient = new ImClient(connectorHost, connectorPort, restUrl);
        imClient.setClientMsgListener(new ClientMsgListener() {
            @Override
            public void online() {
                logger.info("[client] i have connected to server!");
            }

            @Override
            public void read(Chat.ChatMsg chatMsg) {
                //when it's confirmed that user has seen this msg
                readMsg.getAndIncrement();
                chatApi.confirmRead(chatMsg);
            }

            @Override
            public void hasSent(Long id) {
                CompletableFuture<Long> future = hasSentFutureMap.get(id);
                if (future != null) {
                    future.complete(id);
                }
            }

            @Override
            public void hasDelivered(Long id) {
                CompletableFuture<Long> future = hasDeliveredFutureMap.get(id);
                if (future != null) {
                    future.complete(id);
                }
            }

            @Override
            public void hasRead(Long id) {
                CompletableFuture<Long> future = hasReadFutureMap.get(id);
                if (future != null) {
                    future.complete(id);
                }
            }

            @Override
            public void offline() {
                logger.info("[{}] I am offline!", userInfo != null ? userInfo.getUsername() : "client");
            }

            @Override
            public void hasException(ChannelHandlerContext ctx, Throwable cause) {
                logger.error("[" + userInfo.getUsername() + "] has error ", cause);
            }
        });

        imClient.start();

        return imClient;
    }

    void randomSendTest() {
        sendMsg.getAndIncrement();
        int index = ThreadLocalRandom.current().nextInt(0, friends.size());
        String randomText = RandomStringUtils.random(20, true, true);
        Long msgId = IdWorker.genId();

        Chat.ChatMsg chat = chatApi.chatMsgBuilder()
            .setId(msgId)
            .setFromId(userInfo.getId())
            .setDestId(friends.get(index).getUserId())
            .setDestType(Chat.ChatMsg.DestType.SINGLE)
            .setCreateTime(System.currentTimeMillis())
            .setMsgType(Chat.ChatMsg.MsgType.TEXT)
            .setVersion(1)
            .setMsgBody(ByteString.copyFrom(randomText, CharsetUtil.UTF_8))
            .build();

        setFuture(msgId);
        chatApi.send(chat);
    }

    private void setFuture(Long msgId) {
        CompletableFuture<Long> hasSentFuture = new CompletableFuture<>();
        hasSentFuture.whenComplete((id, e) -> {
            if (e != null) {
                hasException.getAndIncrement();
                logger.error("[" + userInfo.getUsername() + "] has error", e);
            } else {
                hasSentAck.getAndIncrement();
                logger.info("[{}]get a msg: {} has been sent", userInfo.getUsername(), id);
            }
        });

        hasSentFutureMap.put(msgId, hasSentFuture);

        CompletableFuture<Long> hasDeliveredFuture = new CompletableFuture<>();
        hasDeliveredFuture.whenComplete((id, e) -> {
            if (e != null) {
                hasException.getAndIncrement();
                logger.error("[" + userInfo.getUsername() + "] has error", e);
            } else {
                hasDeliveredAck.getAndIncrement();
                logger.info("[{}] get a msg: {} has been delivered", userInfo.getUsername(), id);
            }
        });
        hasDeliveredFutureMap.put(msgId, hasDeliveredFuture);

        CompletableFuture<Long> hasReadFuture = new CompletableFuture<>();
        hasReadFuture.whenComplete((id, e) -> {
            if (e != null) {
                hasException.getAndIncrement();
                logger.error("[" + userInfo.getUsername() + "] has error", e);
            } else {
                hasReadAck.getAndIncrement();
                logger.info("[{}] get a msg: {} has been read", userInfo.getUsername(), id);
            }
        });
        hasReadFutureMap.put(msgId, hasReadFuture);
    }
}