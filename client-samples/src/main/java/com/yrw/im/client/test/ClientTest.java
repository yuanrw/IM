package com.yrw.im.client.test;

import com.google.protobuf.ByteString;
import com.yim.im.client.ImClient;
import com.yim.im.client.api.ChatApi;
import com.yim.im.client.api.ClientMsgListener;
import com.yim.im.client.api.UserApi;
import com.yim.im.client.domain.Friend;
import com.yrw.im.common.domain.UserInfo;
import com.yrw.im.common.util.IdWorker;
import com.yrw.im.proto.generate.Chat;
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
class ClientTest {
    private static Logger logger = LoggerFactory.getLogger(ClientTest.class);

    private static ConcurrentMap<Long, CompletableFuture<Long>> hasSentFutureMap = new ConcurrentHashMap<>();
    private static ConcurrentMap<Long, CompletableFuture<Long>> hasDeliveredFutureMap = new ConcurrentHashMap<>();
    private static ConcurrentMap<Long, CompletableFuture<Long>> hasReadFutureMap = new ConcurrentHashMap<>();

    private ChatApi chatApi;
    private String host;
    private Integer port;

    private UserInfo userInfo;

    private List<Friend> friends;

    static AtomicInteger sendMsg = new AtomicInteger(0);
    static AtomicInteger readMsg = new AtomicInteger(0);
    static AtomicInteger hasSentAck = new AtomicInteger(0);
    static AtomicInteger hasDeliveredAck = new AtomicInteger(0);
    static AtomicInteger hasReadAck = new AtomicInteger(0);
    static AtomicInteger hasException = new AtomicInteger(0);

    ClientTest(String host, Integer port, String username, String password) {
        this.host = host;
        this.port = port;
        chatApi = ImClient.getApi(ChatApi.class);
        UserApi userApi = ImClient.getApi(UserApi.class);

        start();
        //登录换取token
        userInfo = userApi.login(username, DigestUtils.sha256Hex(password.getBytes(CharsetUtil.UTF_8)));
        //获取好友列表
        friends = userApi.friends(userInfo.getToken());
    }

    private void start() {
        new ImClient()
            .setConnectorHost(host)
            .setConnectorPort(port)
            .setClientMsgListener(new ClientMsgListener() {
                @Override
                public void online() {
                    logger.info("[client] I am online!");
                }

                @Override
                public void read(Chat.ChatMsg chatMsg) {
                    //when it's confirmed that user has seen this msg
                    readMsg.getAndIncrement();
                    chatApi.confirmRead(chatMsg);
                }

                @Override
                public void hasSent(Long id) {
                    hasSentFutureMap.get(id).complete(id);
                }

                @Override
                public void hasDelivered(Long id) {
                    hasDeliveredFutureMap.get(id).complete(id);
                }

                @Override
                public void hasRead(Long id) {
                    hasReadFutureMap.get(id).complete(id);
                }

                @Override
                public void offline() {
                    logger.info("[client]{} I am offline!", userInfo.getId());
                }

                @Override
                public void hasException(ChannelHandlerContext ctx, Throwable cause) {
                    logger.error("[client] has error ", cause);
                }
            }).start();
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
                logger.error("[client] has error", e);
            } else {
                hasSentAck.getAndIncrement();
                logger.info("[client]{} msg: {} has been sent", userInfo.getUsername(), id);
            }
        });

        hasSentFutureMap.put(msgId, hasSentFuture);

        CompletableFuture<Long> hasDeliveredFuture = new CompletableFuture<>();
        hasDeliveredFuture.whenComplete((id, e) -> {
            if (e != null) {
                hasException.getAndIncrement();
                logger.error("[client] has error", e);
            } else {
                hasDeliveredAck.getAndIncrement();
                logger.info("[client]{} msg: {} has been delivered", userInfo.getUsername(), id);
            }
        });
        hasDeliveredFutureMap.put(msgId, hasDeliveredFuture);

        CompletableFuture<Long> hasReadFuture = new CompletableFuture<>();
        hasReadFuture.whenComplete((id, e) -> {
            if (e != null) {
                hasException.getAndIncrement();
                logger.error("[client] has error", e);
            } else {
                hasReadAck.getAndIncrement();
                logger.info("[client]{} msg: {} has been read", userInfo.getUsername(), id);
            }
        });
        hasReadFutureMap.put(msgId, hasReadFuture);
    }
}