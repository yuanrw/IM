package com.yrw.im.rest.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.yrw.im.common.domain.po.Offline;
import com.yrw.im.proto.generate.Ack;
import com.yrw.im.proto.generate.Chat;

import java.util.List;

/**
 * Date: 2019-05-05
 * Time: 09:48
 *
 * @author yrw
 */
public interface OfflineService extends IService<Offline> {

    /**
     * 保存离线聊天消息
     *
     * @param msg
     */
    void saveChat(Chat.ChatMsg msg);

    /**
     * 保存离线ack消息
     *
     * @param msg
     */
    void saveAck(Ack.AckMsg msg);

    /**
     * 获取某个用户的所有离线消息
     *
     * @param userId
     * @return
     * @throws JsonProcessingException
     */
    List<Offline> pollOfflineMsg(Long userId) throws JsonProcessingException;
}
