package com.yrw.im.offline.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.yrw.im.common.domain.po.Offline;
import com.yrw.im.proto.generate.Chat;
import com.yrw.im.proto.generate.Internal;

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

    Internal.InternalMsg listOfflineMsg(Long relationId) throws JsonProcessingException;
}
