package com.yim.im.client.api;

import com.yrw.im.proto.generate.Chat;

/**
 * Date: 2019-05-18
 * Time: 23:46
 *
 * @author yrw
 */
public interface ClientMsgListener {

    void active();

    void read(Chat.ChatMsg chatMsg);

    void inactive();
}
