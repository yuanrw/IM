package com.github.yuanrw.im.rest.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.yuanrw.im.common.domain.po.Relation;
import com.github.yuanrw.im.common.domain.po.RelationDetail;

import java.util.List;

/**
 * Date: 2019-04-07
 * Time: 18:47
 *
 * @author yrw
 */
public interface RelationService extends IService<Relation> {

    /**
     * return the friends list of the user
     *
     * @param id userId
     * @return
     */
    List<RelationDetail> friends(String id);

    /**
     * add an relation between user1 and user2
     * insure that the same relation can only be saved once.
     * by default, use mysql union unique index.
     * if the db don't support a union unique index, then you need to check the exist relation
     *
     * @param userId1 id of user1l
     * @param userId2 id of user2
     * @return if success, return relation id, else return Mono.empty()
     */
    Long saveRelation(String userId1, String userId2);
}
