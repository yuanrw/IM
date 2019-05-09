package com.yrw.im.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yrw.im.common.domain.Offline;
import com.yrw.im.proto.generate.Chat;

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
    boolean saveChatMsg(Chat.ChatMsg msg);
}
