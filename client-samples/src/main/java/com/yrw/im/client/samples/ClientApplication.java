package com.yrw.im.client.samples;

import com.yim.im.client.Client;
import com.yim.im.client.api.ChatApi;
import com.yim.im.client.api.ClientMsgListener;
import com.yim.im.client.api.UserApi;
import com.yrw.im.common.domain.UserInfo;
import com.yrw.im.common.domain.po.Relation;
import com.yrw.im.proto.generate.Chat;

import java.util.List;

/**
 * Date: 2019-05-15
 * Time: 13:57
 *
 * @author yrw
 */
public class ClientApplication {

    public static void main(String[] args) {
        Client client = new Client()
            .setConnectorHost("127.0.0.1")
            .setConnectorPort(9081)
            .setClientMsgListener(new ClientMsgListener() {
                @Override
                public void active() {
                    System.out.println("user is online!");
                }

                @Override
                public void read(Chat.ChatMsg chatMsg) {
                    System.out.println("do customer function: " + chatMsg.toString());
                }

                @Override
                public void inactive() {
                    System.out.println("user is offline");
                }
            })
            .start();

        UserApi userApi = client.getApi(UserApi.class);
        ChatApi chatApi = client.getApi(ChatApi.class);
        //登录换取token
        UserInfo user = userApi.login("yuanrw", "123abc");

        //获取好友列表
        List<Relation> friends = userApi.relations(user.getUserId(), user.getToken());
        Relation relation = friends.get(0);

        //发送消息
        chatApi.text(user.getUserId(), user.getUserId(), "hello", user.getToken());
    }

    private static Long getFriend(Relation relation, Long userId) {
        return !relation.getUserId1().equals(userId) ? relation.getUserId1() : relation.getUserId2();
    }
}
