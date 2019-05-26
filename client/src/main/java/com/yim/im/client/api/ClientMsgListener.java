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
     * 消费消息
     *
     * @param chatMsg
     */
    void read(Chat.ChatMsg chatMsg);

    /**
     * 已发送
     *
     * @param id chatMsg id
     */
    void hasSent(Long id);

    /**
     * 已送达
     *
     * @param id chatMsg id
     */
    void hasDelivered(Long id);

    /**
     * 已读
     *
     * @param id chatMsg id
     */
    void hasRead(Long id);

    /**
     * 下线
     */
    void offline();
}
