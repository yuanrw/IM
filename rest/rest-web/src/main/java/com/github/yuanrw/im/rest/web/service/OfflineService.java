package com.github.yuanrw.im.rest.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.yuanrw.im.common.domain.po.Offline;
import com.github.yuanrw.im.protobuf.generate.Ack;
import com.github.yuanrw.im.protobuf.generate.Chat;

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
    List<Offline> pollOfflineMsg(String userId) throws JsonProcessingException;
}