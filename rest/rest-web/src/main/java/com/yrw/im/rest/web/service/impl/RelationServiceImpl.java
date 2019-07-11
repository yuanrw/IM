package com.yrw.im.rest.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yrw.im.common.domain.po.Relation;
import com.yrw.im.common.exception.ImException;
import com.yrw.im.rest.spi.UserSpi;
import com.yrw.im.rest.spi.domain.UserBase;
import com.yrw.im.rest.web.mapper.RelationMapper;
import com.yrw.im.rest.web.service.RelationService;
import com.yrw.im.rest.web.util.SpiFactory;
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

    private UserSpi<? extends UserBase> userSpi;

    public RelationServiceImpl(SpiFactory spiFactory) {
        this.userSpi = spiFactory.getUserSpi();
    }

    @Override
    public List<Relation> friends(String id) {
        return list(new LambdaQueryWrapper<Relation>()
            .eq(Relation::getUserId1, id).or().eq(Relation::getUserId2, id));
    }

    @Override
    public Long saveRelation(String userId1, String userId2) {
        if (userId1.equals(userId2)) {
            throw new ImException("[rest] userId1 and userId2 can not be same");
        }
        if (userSpi.getById(userId1 + "") == null || userSpi.getById(userId2 + "") == null) {
            throw new ImException("[rest] user not exist");
        }
        String max = userId1.compareTo(userId2) >= 0 ? userId1 : userId2;
        String min = max.equals(userId1) ? userId2 : userId1;

        Relation relation = new Relation();
        relation.setUserId1(min);
        relation.setUserId2(max);
        relation.setEncryptKey(RandomStringUtils.randomAlphanumeric(16) + "|" + RandomStringUtils.randomNumeric(16));

        if (save(relation)) {
            return relation.getId();
        } else {
            throw new ImException("[rest] save relation failed");
        }
    }
}