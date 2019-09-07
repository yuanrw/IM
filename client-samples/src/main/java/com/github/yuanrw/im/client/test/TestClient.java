package com.github.yuanrw.im.client.test;

import com.github.yuanrw.im.client.ImClient;
import com.github.yuanrw.im.client.api.ChatApi;
import com.github.yuanrw.im.client.api.ClientMsgListener;
import com.github.yuanrw.im.client.api.UserApi;
import com.github.yuanrw.im.client.domain.Friend;
import com.github.yuanrw.im.common.domain.UserInfo;
import com.github.yuanrw.im.common.domain.constant.MsgVersion;
import com.github.yuanrw.im.common.util.IdWorker;
import com.github.yuanrw.im.protobuf.generate.Chat;
import com.google.protobuf.ByteString;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.CharsetUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Date: 2019-07-09
 * Time: 10:08
 *
 * @author yrw
 */
class TestClient {
    static AtomicInteger sendMsg = new AtomicInteger(0);
    static AtomicInteger readMsg = new AtomicInteger(0);
    static AtomicInteger hasSentAck = new AtomicInteger(0);
    static AtomicInteger hasDeliveredAck = new AtomicInteger(0);
    static AtomicInteger hasReadAck = new AtomicInteger(0);
    static AtomicInteger hasException = new AtomicInteger(0);
    private static Logger logger = LoggerFactory.getLogger(TestClient.class);
    private ChatApi chatApi;
    private UserInfo userInfo;
    private List<Friend> friends;

    TestClient(String connectorHost, Integer connectorPort, String restUrl, String username, String password) {
        ImClient imClient = start(connectorHost, connectorPort, restUrl);
        chatApi = imClient.chatApi();
        UserApi userApi = imClient.userApi();

        //login and get a token
        userInfo = userApi.login(username, DigestUtils.sha256Hex(password.getBytes(CharsetUtil.UTF_8)));
        //get friends list
        friends = userApi.friends(userInfo.getToken());
    }

    private ImClient start(String connectorHost, Integer connectorPort, String restUrl) {
        ImClient imClient = new ImClient(connectorHost, connectorPort, restUrl);
        imClient.setClientMsgListener(new ClientMsgListener() {
            @Override
            public void online() {
                logger.info("[client] i have connected to server!");
            }

            @Override
            public void read(Chat.ChatMsg chatMsg) {
                //when it's confirmed that user has seen this msg
                logger.info("[{}] get a msg: {}", userInfo.getUsername(), chatMsg.toString());
                readMsg.getAndIncrement();
                chatApi.confirmRead(chatMsg);
            }

            @Override
            public void hasSent(Long id) {
                hasSentAck.getAndIncrement();
                logger.info("[{}] get a msg: {} has been sent", userInfo.getUsername(), id);
            }

            @Override
            public void hasDelivered(Long id) {
                hasDeliveredAck.getAndIncrement();
                logger.info("[{}] get a msg: {} has been delivered", userInfo.getUsername(), id);
            }

            @Override
            public void hasRead(Long id) {
                hasReadAck.getAndIncrement();
                logger.info("[{}] get a msg: {} has been read", userInfo.getUsername(), id);
            }

            @Override
            public void offline() {
                logger.info("[{}] I am offline!", userInfo != null ? userInfo.getUsername() : "client");
            }

            @Override
            public void hasException(ChannelHandlerContext ctx, Throwable cause) {
                hasException.getAndIncrement();
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
            .setVersion(MsgVersion.V1.getVersion())
            .setMsgBody(ByteString.copyFrom(randomText, CharsetUtil.UTF_8))
            .build();

        chatApi.send(chat);
    }
}