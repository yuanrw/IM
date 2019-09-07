package com.github.yuanrw.im.client.sample;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Date: 2019-07-09
 * Time: 10:08
 *
 * @author yrw
 */
public class MyClient {
    private static Logger logger = LoggerFactory.getLogger(MyClient.class);

    private ChatApi chatApi;
    private UserInfo userInfo;

    private Map<String, Friend> friendMap;

    public MyClient(String connectorHost, Integer connectorPort, String restUrl, String username, String password) {
        ImClient imClient = start(connectorHost, connectorPort, restUrl);
        chatApi = imClient.chatApi();
        UserApi userApi = imClient.userApi();

        //login and get a token
        userInfo = userApi.login(username, DigestUtils.sha256Hex(password.getBytes(CharsetUtil.UTF_8)));
        //get friends list
        List<Friend> friends = userApi.friends(userInfo.getToken());
        friendMap = friends.stream().collect(Collectors.toMap(Friend::getUserId, f -> f));

        System.out.println("Here are my friends!");
        for (Friend friend : friends) {
            System.out.println(friend.getUserId() + ": " + friend.getUsername());
        }
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
                //when it's confirmed that user has read this msg
                System.out.println(friendMap.get(chatMsg.getFromId()).getUsername() + ": "
                    + chatMsg.getMsgBody().toStringUtf8());
                chatApi.confirmRead(chatMsg);
            }

            @Override
            public void hasSent(Long id) {
                System.out.println(String.format("msg {%d} has been sent", id));
            }

            @Override
            public void hasDelivered(Long id) {
                System.out.println(String.format("msg {%d} has been delivered", id));
            }

            @Override
            public void hasRead(Long id) {
                System.out.println(String.format("msg {%d} has been read", id));
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

    public void printUserInfo() {
        System.out.println("id: " + userInfo.getId());
        System.out.println("username: " + userInfo.getUsername());
    }

    public void send(String id, String text) {
        if (!friendMap.containsKey(id)) {
            System.out.println("friend " + id + " not found!");
            return;
        }
        Chat.ChatMsg chat = chatApi.chatMsgBuilder()
            .setId(IdWorker.genId())
            .setFromId(userInfo.getId())
            .setDestId(id)
            .setDestType(Chat.ChatMsg.DestType.SINGLE)
            .setCreateTime(System.currentTimeMillis())
            .setMsgType(Chat.ChatMsg.MsgType.TEXT)
            .setVersion(MsgVersion.V1.getVersion())
            .setMsgBody(ByteString.copyFrom(text, CharsetUtil.UTF_8))
            .build();

        chatApi.send(chat);
    }
}