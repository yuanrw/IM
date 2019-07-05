package com.yim.im.client.context;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.yim.im.client.service.ClientRestService;
import com.yrw.im.common.domain.po.Relation;

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

    private ConcurrentMap<String, Relation> relationMap;
    private ClientRestService clientRestService;

    @Inject
    public MemoryRelationCache(ClientRestService clientRestService) {
        this.clientRestService = clientRestService;
        this.relationMap = new ConcurrentHashMap<>();
    }

    @Override
    public void addRelations(List<Relation> relations) {
        relationMap.putAll(relations.stream().collect(Collectors.toMap(
            r -> generateKey(r.getUserId1(), r.getUserId2()),
            r -> r)
        ));
    }

    @Override
    public void addRelation(Relation relation) {
        relationMap.put(generateKey(relation.getUserId1(), relation.getUserId2()), relation);
    }

    @Override
    public Relation getRelation(Long userId1, Long userId2, String token) {
        Relation relation = relationMap.get(generateKey(userId1, userId2));
        if (relation == null) {
            relation = getRelationFromRest(userId1, userId2, token);
            relationMap.put(generateKey(userId1, userId2), relation);
        }
        return relation;
    }

    private Relation getRelationFromRest(Long userId1, Long userId2, String token) {
        return clientRestService.relation(userId1, userId2, token);
    }

    private String generateKey(Long userId1, Long userId2) {
        return Math.min(userId1, userId2) + "_" + Math.max(userId1, userId2);
    }
}
