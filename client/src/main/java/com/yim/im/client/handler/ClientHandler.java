package com.yim.im.client.handler;

import com.google.inject.Inject;
import com.google.protobuf.Message;
import com.yim.im.client.service.ChatService;
import com.yim.im.client.service.RestService;
import com.yrw.im.common.domain.Relation;
import com.yrw.im.common.domain.UserInfo;
import com.yrw.im.common.exception.ImException;
import com.yrw.im.proto.generate.Chat;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

/**
 * Date: 2019-04-15
 * Time: 16:42
 *
 * @author yrw
 */
public class ClientHandler extends SimpleChannelInboundHandler<Message> {

    private Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    private ChatService chatService;
    private RestService restService;

    @Inject
    public ClientHandler(ChatService chatService, RestService restService) {
        this.chatService = chatService;
        this.restService = restService;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //登录换取token
        UserInfo user = restService.login("yuanrw", "123abc");

        chatService.setCtx(ctx);

        //向connector发送greet消息
        chatService.greet(user.getUserId(), ctx);

        //获取好友列表
        List<Relation> friends = restService.friends(user.getUserId(), user.getToken());

        //随机选择好友发送消息
        if (friends.size() > 0) {
            int index = new Random().nextInt(friends.size());
            Long randomFriend = getFriend(friends.get(index), user.getUserId());
            chatService.text(user.getUserId(), randomFriend, "hello", user.getToken());
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        Chat.ChatMsg chat = (Chat.ChatMsg) msg;
        logger.info("[client] receive msg: {}", chat.getMsgBody().toString(CharsetUtil.UTF_8));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("[client] has error: ", cause);
        throw new ImException("client has error");
    }

    private Long getFriend(Relation relation, Long userId) {
        return !relation.getUserId1().equals(userId) ? relation.getUserId1() : relation.getUserId2();
    }
}
