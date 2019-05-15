package com.yrw.im.rest.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.yrw.im.common.domain.po.Offline;
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
     * 保存离线消息
     *
     * @param msg
     */
    void saveChatMsg(Chat.ChatMsg msg);

    List<Chat.ChatMsg> listOfflineMsg(Long userId) throws JsonProcessingException;
}
