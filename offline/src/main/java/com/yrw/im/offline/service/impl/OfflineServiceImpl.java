package com.yrw.im.repository.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.inject.Inject;
import com.yrw.im.common.domain.Offline;
import com.yrw.im.common.domain.Relation;
import com.yrw.im.common.util.Encryptor;
import com.yrw.im.proto.generate.Chat;
import com.yrw.im.repository.mapper.OfflineMapper;
import com.yrw.im.repository.service.OfflineService;
import com.yrw.im.repository.service.RelationService;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Date: 2019-05-05
 * Time: 09:49
 *
 * @author yrw
 */
@Service
public class OfflineServiceImpl extends ServiceImpl<OfflineMapper, Offline> implements OfflineService {

    private RelationService relationService;

    @Inject
    public OfflineServiceImpl(RelationService relationService) {
        this.relationService = relationService;
    }

    @Override
    public boolean saveChatMsg(Chat.ChatMsg msg) {
        Long userId1 = Math.min(msg.getFromId(), msg.getDestId());
        Long userId2 = Math.max(msg.getFromId(), msg.getDestId());
        Relation relation = relationService.getOne(new LambdaQueryWrapper<Relation>()
            .eq(Relation::getUserId1, userId1).eq(Relation::getUserId2, userId2));

        String[] keys = relation.getEncryptKey().split("|");

        String content = Encryptor.encrypt(keys[0], keys[1], msg.toByteArray());

        Offline offline = new Offline();
        offline.setConversationId(relation.getConversationId());
        offline.setGmtMsgCreate(new Date(msg.getCreateTime()));
        offline.setContent(content);
        return save(offline);
    }
}
