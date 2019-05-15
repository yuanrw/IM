package com.yrw.im.rest.repository.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yrw.im.common.domain.po.Relation;
import com.yrw.im.rest.repository.mapper.RelationMapper;
import com.yrw.im.rest.repository.service.RelationService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Date: 2019-04-07
 * Time: 18:48
 *
 * @author yrw
 */
@Service
public class RelationServiceImpl extends ServiceImpl<RelationMapper, Relation> implements RelationService {

    @Override
    public List<Relation> friends(Long id) {
        return list(new LambdaQueryWrapper<Relation>()
            .eq(Relation::getUserId1, id).or().eq(Relation::getUserId2, id));
    }

    @Override
    public boolean addRelation(Long userId1, Long userId2) {
        Relation relation = new Relation();
        relation.setUserId1(Math.min(userId1, userId2));
        relation.setUserId2(Math.max(userId1, userId2));
        relation.setEncryptKey(RandomStringUtils.randomAlphanumeric(16) + "|" + RandomStringUtils.randomNumeric(16));

        return save(relation);
    }
}
