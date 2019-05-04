package com.yrw.im.repository.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yrw.im.common.domain.Relation;
import com.yrw.im.repository.mapper.RelationMapper;
import com.yrw.im.repository.service.RelationService;
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
}
