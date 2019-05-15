package com.yim.im.client.api;

import com.google.inject.Inject;
import com.yim.im.client.Client;
import com.yim.im.client.domain.RelationCache;
import com.yim.im.client.handler.ClientHandler;
import com.yim.im.client.service.ClientRestService;
import com.yrw.im.common.domain.UserInfo;
import com.yrw.im.common.domain.po.Relation;
import com.yrw.im.proto.generate.Internal;

import java.util.List;

/**
 * Date: 2019-05-14
 * Time: 10:29
 *
 * @author yrw
 */
public class UserApi {

    private ClientRestService clientRestService;
    private RelationCache relationCache;

    @Inject
    public UserApi(ClientRestService clientRestService) {
        this.relationCache = Client.injector.getInstance(RelationCache.class);
        this.clientRestService = clientRestService;
    }

    public UserInfo login(String username, String password) {
        UserInfo userInfo = clientRestService.login(username, password);
        userLoginInit(userInfo.getUserId());
        return userInfo;
    }

    private void userLoginInit(Long userId) {
        Internal.InternalMsg greet = Internal.InternalMsg.newBuilder()
            .setFrom(Internal.InternalMsg.Module.CLIENT)
            .setDest(Internal.InternalMsg.Module.CONNECTOR)
            .setCreateTime(System.currentTimeMillis())
            .setVersion(1)
            .setMsgType(Internal.InternalMsg.InternalMsgType.GREET)
            .setMsgBody(String.valueOf(userId))
            .build();

        ClientHandler.getCtx().writeAndFlush(greet);

        //todo: 此处应该确认服务端已经处理化完毕
    }

    public Void logout(String token) {
        return clientRestService.logout(token);
    }

    public List<Relation> relations(Long userId, String token) {
        return clientRestService.friends(userId, token);
    }

    public Relation relation(Long userId1, Long userId2, String token) {
        Relation relation = relationCache.get(userId1, userId2);
        return relation != null ? relation : clientRestService.relation(userId1, userId2, token);
    }
}
