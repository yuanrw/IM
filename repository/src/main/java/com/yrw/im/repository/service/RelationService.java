package com.yrw.im.repository.service;

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
     * 返回用户的朋友列表
     *
     * @param id 用户id
     * @return
     */
    List<Relation> friends(Long id);

    /**
     * 添加关系
     *
     * @param userId1
     * @param userId2
     * @return
     */
    boolean addRelation(Long userId1, Long userId2);
}
