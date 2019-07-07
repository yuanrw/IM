package com.yrw.im.rest.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yrw.im.common.domain.po.Relation;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
    Flux<Relation> friends(String id);

    /**
     * add an relation between user1 and user2
     *
     * @param userId1 id of user1l
     * @param userId2 id of user2
     * @return if success, return relation id, else return Mono.empty()
     */
    Mono<Long> saveRelation(String userId1, String userId2);
}
