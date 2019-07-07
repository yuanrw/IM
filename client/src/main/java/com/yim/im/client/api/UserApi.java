package com.yim.im.client.api;

import com.google.inject.Inject;
import com.yim.im.client.context.UserContext;
import com.yim.im.client.domain.Friend;
import com.yim.im.client.handler.ClientConnectorHandler;
import com.yim.im.client.service.ClientRestService;
import com.yrw.im.common.domain.UserInfo;
import com.yrw.im.common.domain.po.Relation;
import com.yrw.im.common.exception.ImException;
import com.yrw.im.common.util.IdWorker;
import com.yrw.im.proto.generate.Internal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Date: 2019-05-14
 * Time: 10:29
 *
 * @author yrw
 */
public class UserApi {
    private Logger logger = LoggerFactory.getLogger(UserApi.class);

    private ClientRestService clientRestService;
    private UserContext userContext;

    @Inject
    public UserApi(ClientRestService clientRestService, UserContext userContext) {
        this.clientRestService = clientRestService;
        this.userContext = userContext;
    }

    public UserInfo login(String username, String password) {
        UserInfo userInfo = clientRestService.login(username, password);
        //等待connector的ack信息
        userLoginInit(userInfo.getId());

        assert userInfo.getId() != null;

        userContext.setUserId(userInfo.getId());
        userContext.setToken(userInfo.getToken());
        userContext.addRelations(userInfo.getRelations());
        return userInfo;
    }

    private void userLoginInit(String userId) {
        Internal.InternalMsg greet = Internal.InternalMsg.newBuilder()
            .setId(IdWorker.genId())
            .setFrom(Internal.InternalMsg.Module.CLIENT)
            .setDest(Internal.InternalMsg.Module.CONNECTOR)
            .setCreateTime(System.currentTimeMillis())
            .setVersion(1)
            .setMsgType(Internal.InternalMsg.MsgType.GREET)
            .setMsgBody(userId)
            .build();


        CompletableFuture<Internal.InternalMsg> future = ClientConnectorHandler.createCollector(Duration.ofSeconds(10)).getFuture()
            .whenComplete((m, e) -> {
                if (!m.getMsgBody().equals(greet.getId() + "")) {
                    throw new ImException("[client] user connected to server failed, " +
                        "init msg id is: {}, but received ack id is: {}");
                } else {
                    logger.info("[client] client connect to server successfully");
                }
            });

        ClientConnectorHandler.getCtx().writeAndFlush(greet);

        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ImException("[client] waiting for connector's response failed", e);
        }
    }

    public Void logout(String token) {
        return clientRestService.logout(token);
    }

    public List<Friend> friends(String userId, String token) {
        return getFriend(clientRestService.friends(userId, token), userId);
    }

    private static List<Friend> getFriend(List<Relation> relations, String userId) {
        return relations.stream().map(r -> {
            Friend friend = new Friend();
            String friendId = !r.getUserId1().equals(userId) ? r.getUserId1() : r.getUserId2();
            friend.setUserId(friendId);
            friend.setEncryptKey(r.getEncryptKey());
            return friend;
        }).collect(Collectors.toList());
    }
}
