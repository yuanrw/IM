package com.yrw.im.rest.repository.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.protobuf.TextFormat;
import com.yrw.im.common.domain.po.Offline;
import com.yrw.im.common.exception.ImException;
import com.yrw.im.proto.generate.Chat;
import com.yrw.im.rest.repository.mapper.OfflineMapper;
import com.yrw.im.rest.repository.service.OfflineService;
import org.springframework.stereotype.Service;

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
    public void saveChatMsg(Chat.ChatMsg msg) {
        Offline offline = new Offline();
        offline.setFromUserId(msg.getFromId());
        offline.setToUserId(msg.getDestId());
        offline.setContent(msg.toString());

        if (!save(offline)) {
            throw new ImException("[offline] save chat msg failed");
        }
    }

    @Override
    public List<Chat.ChatMsg> listOfflineMsg(Long userId) {
        List<Offline> offlineMsgList = list(new LambdaQueryWrapper<Offline>()
            .eq(Offline::getToUserId, userId));

        return offlineMsgList.stream()
            .map(o -> {
                try {
                    Chat.ChatMsg.Builder builder = Chat.ChatMsg.newBuilder();
                    TextFormat.getParser().merge(o.getContent(), builder);
                    return builder.build();
                } catch (TextFormat.ParseException e) {
                    throw new ImException("[offline] get offline msg failed", e);
                }
            }).collect(Collectors.toList());
    }
}
