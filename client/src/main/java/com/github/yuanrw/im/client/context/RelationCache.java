package com.github.yuanrw.im.client.context;

import com.github.yuanrw.im.common.domain.po.RelationDetail;

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
    void addRelations(List<RelationDetail> relations);

    /**
     * add a relation
     *
     * @param relation
     */
    void addRelation(RelationDetail relation);

    /**
     * get relation by userId
     *
     * @param userId1
     * @param userId2
     * @param token
     * @return
     */
    RelationDetail getRelation(String userId1, String userId2, String token);
}
