package com.github.yuanrw.im.rest.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yuanrw.im.common.domain.po.Relation;
import com.github.yuanrw.im.common.domain.po.RelationDetail;
import com.github.yuanrw.im.common.exception.ImException;
import com.github.yuanrw.im.rest.spi.UserSpi;
import com.github.yuanrw.im.rest.spi.domain.UserBase;
import com.github.yuanrw.im.rest.web.mapper.RelationMapper;
import com.github.yuanrw.im.rest.web.service.RelationService;
import com.github.yuanrw.im.rest.web.spi.SpiFactory;
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
    public List<RelationDetail> friends(String id) {
        return baseMapper.listFriends(id);
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