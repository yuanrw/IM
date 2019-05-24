package com.yim.im.client.api;

import com.yrw.im.proto.generate.Chat;

/**
 * Date: 2019-05-18
 * Time: 23:46
 *
 * @author yrw
 */
public interface ClientMsgListener {

    /**
     * 上线成功
     */
    void online();

    /**
     * 获取消息
     *
     * @param chatMsg
     */
    void read(Chat.ChatMsg chatMsg);

    /**
     * 下线
     */
    void offline();
}
