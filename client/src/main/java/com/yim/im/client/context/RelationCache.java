package com.yim.im.client.context;

import com.yrw.im.common.domain.po.Relation;

import java.util.List;

/**
 * Date: 2019-07-03
 * Time: 16:24
 *
 * @author yrw
 */
public interface RelationCache {

    /**
     * add multiple relations
     *
     * @param relations
     */
    void addRelations(List<Relation> relations);

    /**
     * add a relation
     *
     * @param relation
     */
    void addRelation(Relation relation);

    /**
     * get relation by userId
     *
     * @param userId1
     * @param userId2
     * @param token
     * @return
     */
    Relation getRelation(String userId1, String userId2, String token);
}
