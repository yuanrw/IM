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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
    public Flux<Relation> friends(String id) {
        return Flux.fromIterable(list(new LambdaQueryWrapper<Relation>()
            .eq(Relation::getUserId1, id).or().eq(Relation::getUserId2, id)));
    }

    @Override
    public Mono<Long> saveRelation(String userId1, String userId2) {
        return userSpi.getById(userId1)
            .flatMap(ignore -> userSpi.getById(userId2))
            .switchIfEmpty(Mono.error(new ImException("user not exist")))
            .map(ignore -> {
                Relation relation = new Relation();

                String max = userId1.compareTo(userId2) >= 0 ? userId1 : userId2;
                String min = max.equals(userId1) ? userId2 : userId1;

                relation.setUserId1(min);
                relation.setUserId2(max);
                relation.setEncryptKey(RandomStringUtils.randomAlphanumeric(16) + "|" + RandomStringUtils.randomNumeric(16));
                return save(relation) ? relation.getId() : null;
            })
            .flatMap(id -> id != null ? Mono.just(id) : Mono.error(new ImException("[rest] save relation failed")));
    }
}
