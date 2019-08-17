package com.github.yuanrw.im.rest.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.yuanrw.im.common.domain.po.Relation;
import com.github.yuanrw.im.common.domain.po.RelationDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Date: 2019-02-11
 * Time: 17:21
 *
 * @author yrw
 */
public interface RelationMapper extends BaseMapper<Relation> {

    /**
     * list user's friends
     *
     * @param userId
     * @return
     */
    List<RelationDetail> listFriends(@Param("userId") String userId);
}
