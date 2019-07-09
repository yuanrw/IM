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
     * save offline chat msg
     *
     * @param msg
     * @return
     */
    void saveChat(Chat.ChatMsg msg);

    /**
     * save offline ack msg
     *
     * @param msg
     * @return
     */
    void saveAck(Ack.AckMsg msg);

    /**
     * get a user's all offline msgs
     *
     * @param userId
     * @return
     * @throws JsonProcessingException
     */
    List<Offline> pollOfflineMsg(Long userId) throws JsonProcessingException;
}