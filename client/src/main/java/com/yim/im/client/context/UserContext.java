package com.yim.im.client.context;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.yim.im.client.handler.ClientConnectorHandler;
import com.yrw.im.common.domain.po.Relation;

import java.util.List;

/**
 * user's info
 * Date: 2019-07-03
 * Time: 15:25
 *
 * @author yrw
 */
@Singleton
public class UserContext {

    private Long userId;

    private String token;

    private RelationCache relationCache;

    private ClientConnectorHandler clientConnectorHandler;

    @Inject
    public UserContext(RelationCache relationCache) {
        this.relationCache = relationCache;
    }

    public ClientConnectorHandler getClientConnectorHandler() {
        return clientConnectorHandler;
    }

    public void setClientConnectorHandler(ClientConnectorHandler clientConnectorHandler) {
        this.clientConnectorHandler = clientConnectorHandler;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public void addRelations(List<Relation> relations) {
        relationCache.addRelations(relations);
    }

    public void addRelation(Relation relation) {
        relationCache.addRelation(relation);
    }

    public Relation getRelation(Long userId1, Long userId2) {
        return relationCache.getRelation(userId1, userId2, token);
    }
}
