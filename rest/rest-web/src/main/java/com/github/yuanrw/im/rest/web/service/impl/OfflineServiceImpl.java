package com.github.yuanrw.im.rest.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yuanrw.im.common.domain.po.DbModel;
import com.github.yuanrw.im.common.domain.po.Offline;
import com.github.yuanrw.im.common.exception.ImException;
import com.github.yuanrw.im.protobuf.constant.MsgTypeEnum;
import com.github.yuanrw.im.protobuf.generate.Ack;
import com.github.yuanrw.im.protobuf.generate.Chat;
import com.github.yuanrw.im.rest.web.mapper.OfflineMapper;
import com.github.yuanrw.im.rest.web.service.OfflineService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Date: 2019-05-05
 * Time: 09:49
 *
 * @author yrw
 */
@Service
public class OfflineServiceImpl extends ServiceImpl<OfflineMapper, Offline> implements OfflineService {

    @Override
    public void saveChat(Chat.ChatMsg msg) {
        Offline offline = new Offline();
        offline.setMsgId(msg.getId());
        offline.setMsgCode(MsgTypeEnum.CHAT.getCode());
        offline.setToUserId(msg.getDestId());
        offline.setContent(msg.toByteArray());

        saveOffline(offline);
    }

    @Override
    public void saveAck(Ack.AckMsg msg) {
        Offline offline = new Offline();
        offline.setMsgId(msg.getId());
        offline.setMsgCode(MsgTypeEnum.getByClass(Ack.AckMsg.class).getCode());
        offline.setToUserId(msg.getDestId());
        offline.setContent(msg.toByteArray());

        saveOffline(offline);
    }

    private void saveOffline(Offline offline) {
        if (!save(offline)) {
            throw new ImException("[offline] save chat msg failed");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Offline> pollOfflineMsg(String userId) {
        List<Offline> unreadList = list(new LambdaQueryWrapper<Offline>()
            .eq(Offline::getToUserId, userId));

        return unreadList.stream().filter(offline ->
            baseMapper.readMsg(offline.getId()) > 0)
            .sorted(Comparator.comparing(DbModel::getId))
            .collect(Collectors.toList());
    }
}