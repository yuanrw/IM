package com.github.yuanrw.im.client.context;

import com.github.yuanrw.im.client.handler.ClientConnectorHandler;
import com.github.yuanrw.im.common.domain.po.Relation;
import com.github.yuanrw.im.common.domain.po.RelationDetail;
import com.google.inject.Inject;

import java.util.List;

/**
 * user's info
 * Date: 2019-07-03
 * Time: 15:25
 *
 * @author yrw
 */
public class UserContext {

    private String userId;

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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public RelationCache getRelationCache() {
        return relationCache;
    }

    public void setRelationCache(RelationCache relationCache) {
        this.relationCache = relationCache;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void addRelations(List<RelationDetail> relations) {
        relationCache.addRelations(relations);
    }

    public void addRelation(RelationDetail relation) {
        relationCache.addRelation(relation);
    }

    public Relation getRelation(String userId1, String userId2) {
        return relationCache.getRelation(userId1, userId2, token);
    }
}
