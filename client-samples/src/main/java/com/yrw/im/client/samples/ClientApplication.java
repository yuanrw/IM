package com.yrw.im.client.samples;

import com.yim.im.client.Client;
import com.yim.im.client.api.ChatApi;
import com.yim.im.client.api.ClientMsgListener;
import com.yim.im.client.api.UserApi;
import com.yim.im.client.domain.Friend;
import com.yrw.im.common.domain.UserInfo;
import com.yrw.im.proto.generate.Chat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Date: 2019-05-15
 * Time: 13:57
 *
 * @author yrw
 */
public class ClientApplication {
    private static Logger logger = LoggerFactory.getLogger(ClientApplication.class);

    public static void main(String[] args) {
        ChatApi chatApi = Client.getApi(ChatApi.class);
        UserApi userApi = Client.getApi(UserApi.class);

        new Client()
            .setConnectorHost("127.0.0.1")
            .setConnectorPort(9081)
            .setClientMsgListener(new ClientMsgListener() {
                @Override
                public void online() {
                    logger.info("[client] I am online!");
                }

                @Override
                public void read(Chat.ChatMsg chatMsg) {
                    logger.info("[client] read a msg: {}", chatMsg.toString());

                    //when it's confirmed that user has seen this msg
                    chatApi.confirmRead(chatMsg);
                }

                @Override
                public void hasSent(Long id) {
                    logger.info("[client] msg: {} has been sent", id);
                }

                @Override
                public void hasDelivered(Long id) {
                    logger.info("[client] msg: {} has been delivered", id);
                }

                @Override
                public void hasRead(Long id) {
                    logger.info("[client] msg: {} has been read", id);
                }

                @Override
                public void offline() {
                    logger.info("[client] I am offline!");
                }
            })
            .start();

        //登录换取token
        UserInfo user = userApi.login("yuanrw", "123abc");

        //获取好友列表
        List<Friend> friends = userApi.friends(user.getUserId(), user.getToken());

        //发送消息
        chatApi.text(user.getUserId(), user.getUserId(), "hello", user.getToken());
    }
}
