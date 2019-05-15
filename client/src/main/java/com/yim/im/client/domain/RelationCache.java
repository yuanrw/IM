package com.yim.im.client.domain;

import com.google.inject.Singleton;
import com.yrw.im.common.domain.po.Relation;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Date: 2019-05-15
 * Time: 11:34
 *
 * @author yrw
 */
@Singleton
public class RelationCache {

    private ConcurrentMap<String, Relation> relationMap;

    public RelationCache() {
        this.relationMap = new ConcurrentHashMap<>();
    }

    public void set(Relation relation) {
        relationMap.put(relation.getUserId1() + "_" + relation.getUserId2(), relation);
    }

    public Relation get(Long userId1, Long userId2) {
        return relationMap.get(userId1 + "_" + userId2);
    }
}
