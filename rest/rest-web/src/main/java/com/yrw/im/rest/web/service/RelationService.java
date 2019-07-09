package com.yrw.im.rest.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yrw.im.common.domain.po.Relation;

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
    List<Relation> friends(String id);

    /**
     * add an relation between user1 and user2
     *
     * @param userId1 id of user1l
     * @param userId2 id of user2
     * @return if success, return relation id, else return Mono.empty()
     */
    Long saveRelation(String userId1, String userId2);
}
