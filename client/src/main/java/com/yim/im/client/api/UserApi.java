package com.yim.im.client.api;

import com.google.inject.Inject;
import com.yim.im.client.Client;
import com.yim.im.client.domain.RelationCache;
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

/**
 * Date: 2019-05-14
 * Time: 10:29
 *
 * @author yrw
 */
public class UserApi {
    private Logger logger = LoggerFactory.getLogger(UserApi.class);

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
            .setId(IdWorker.genId())
            .setFrom(Internal.InternalMsg.Module.CLIENT)
            .setDest(Internal.InternalMsg.Module.CONNECTOR)
            .setCreateTime(System.currentTimeMillis())
            .setVersion(1)
            .setMsgType(Internal.InternalMsg.InternalMsgType.GREET)
            .setMsgBody(String.valueOf(userId))
            .build();


        CompletableFuture<Internal.InternalMsg> future = ClientConnectorHandler.createCollector(Duration.ofSeconds(10)).getFuture()
            .whenComplete((m, e) -> {
                if (!m.getMsgBody().equals(greet.getId() + "")) {
                    throw new ImException("[Client] user connected to server failed, " +
                        "init msg id is: {}, but received ack id is: {}");
                } else {
                    logger.info("[Client] user connected to server success");
                }
            });

        ClientConnectorHandler.getCtx().writeAndFlush(greet);

        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ImException("");
        }
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
