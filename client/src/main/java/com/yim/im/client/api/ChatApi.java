package com.yim.im.client.api;

import com.google.inject.Inject;
import com.yim.im.client.service.ChatService;

/**
 * Date: 2019-05-14
 * Time: 10:29
 *
 * @author yrw
 */
public class ChatApi {

    private ChatService chatService;

    @Inject
    public ChatApi(ChatService chatService) {
        this.chatService = chatService;
    }

    public void text(Long userId, Long toId, String text, String token) {
        chatService.text(userId, toId, text, token);
    }

    public void file(Long userId, Long toId, byte[] bytes) {
        chatService.file(userId, toId, bytes);
    }

    public void ack(Long userId, Long fromId, Long id) {
        chatService.ack(userId, fromId, id);
    }
}
