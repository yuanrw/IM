package com.github.yuanrw.im.client.context.impl;

import com.github.yuanrw.im.client.context.RelationCache;
import com.github.yuanrw.im.client.service.ClientRestService;
import com.github.yuanrw.im.common.domain.po.RelationDetail;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * store relation in memory
 * Date: 2019-07-03
 * Time: 16:25
 *
 * @author yrw
 */
@Singleton
public class MemoryRelationCache implements RelationCache {

    private ConcurrentMap<String, RelationDetail> relationMap;
    private ClientRestService clientRestService;

    @Inject
    public MemoryRelationCache(ClientRestService clientRestService) {
        this.clientRestService = clientRestService;
        this.relationMap = new ConcurrentHashMap<>();
    }

    @Override
    public void addRelations(List<RelationDetail> relations) {
        relationMap.putAll(relations.stream().collect(Collectors.toMap(
            r -> generateKey(r.getUserId1(), r.getUserId2()),
            r -> r)
        ));
    }

    @Override
    public void addRelation(RelationDetail relation) {
        relationMap.put(generateKey(relation.getUserId1(), relation.getUserId2()), relation);
    }

    @Override
    public RelationDetail getRelation(String userId1, String userId2, String token) {
        RelationDetail relation = relationMap.get(generateKey(userId1, userId2));
        if (relation == null) {
            relation = getRelationFromRest(userId1, userId2, token);
            if (relation != null) {
                relationMap.put(generateKey(userId1, userId2), relation);
            }
        }
        return relation;
    }

    private RelationDetail getRelationFromRest(String userId1, String userId2, String token) {
        return clientRestService.relation(userId1, userId2, token);
    }

    private String generateKey(String userId1, String userId2) {
        String max = userId1.compareTo(userId2) >= 0 ? userId1 : userId2;
        String min = max.equals(userId1) ? userId2 : userId1;
        return min + "_" + max;
    }
}
