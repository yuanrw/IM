package com.github.yuanrw.im.connector.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.protobuf.Message;
import com.github.yuanrw.im.common.domain.UserStatus;
import com.github.yuanrw.im.common.exception.ImException;
import com.github.yuanrw.im.common.util.IdWorker;
import com.github.yuanrw.im.connector.domain.ClientConn;
import com.github.yuanrw.im.connector.domain.ClientConnContext;
import com.github.yuanrw.im.connector.handler.ConnectorTransferHandler;
import com.github.yuanrw.im.protobuf.constant.UserStatusEnum;
import com.github.yuanrw.im.protobuf.generate.Internal;
import io.netty.channel.ChannelHandlerContext;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Date: 2019-05-14
 * Time: 09:53
 *
 * @author yrw
 */
public class UserStatusService {

    private ClientConnContext clientConnContext;
    private OfflineService offlineService;
    private ObjectMapper objectMapper;

    @Inject
    public UserStatusService(OfflineService offlineService, ClientConnContext clientConnContext) {
        this.clientConnContext = clientConnContext;
        this.offlineService = offlineService;
        this.objectMapper = new ObjectMapper();
    }

    public void userOnline(Long msgId, String userId, ChannelHandlerContext ctx) throws JsonProcessingException, ExecutionException, InterruptedException {
        //保存连接
        ClientConn conn = new ClientConn(ctx);
        conn.setUserId(userId);

        clientConnContext.addConn(conn);

        //向transfer同步用户状态
        UserStatus userStatus = new UserStatus();
        userStatus.setUserId(userId);
        userStatus.setStatus(UserStatusEnum.ONLINE.getCode());

        Internal.InternalMsg status = statusMsg(userStatus);

        CompletableFuture<Internal.InternalMsg> future = ConnectorTransferHandler
            .createUserStatusMsgCollector(Duration.ofSeconds(10))
            .getFuture()
            .whenComplete((m, e) -> {
                if (!m.getMsgBody().equals(status.getId() + "")) {
                    throw new ImException("[client] connect to connector failed, " +
                        "init msg id is: {}, but received ack id is: {}");
                } else {
                    sendAckToClient(msgId, ctx);

                    //发送离线消息
                    List<Message> msgs = offlineService.pollOfflineMsg(userId);
                    msgs.forEach(ctx::write);

                    ctx.flush();
                }
            });

        ConnectorTransferHandler.getCtx().writeAndFlush(status);

        future.get();
    }

    private void sendAckToClient(Long id, ChannelHandlerContext ctx) {
        Internal.InternalMsg ack = Internal.InternalMsg.newBuilder()
            .setId(IdWorker.genId())
            .setVersion(1)
            .setFrom(Internal.InternalMsg.Module.CONNECTOR)
            .setDest(Internal.InternalMsg.Module.CLIENT)
            .setCreateTime(System.currentTimeMillis())
            .setMsgType(Internal.InternalMsg.MsgType.ACK)
            .setMsgBody(id + "")
            .build();

        ctx.writeAndFlush(ack);
    }

    public void userOffline(ChannelHandlerContext ctx) throws JsonProcessingException {
        ClientConn conn = clientConnContext.getConn(ctx);
        if (conn == null) {
            return;
        }

        //tell the transfer the user is offline
        UserStatus userStatus = new UserStatus();
        userStatus.setUserId(conn.getUserId());
        userStatus.setStatus(UserStatusEnum.OFFLINE.getCode());

        ConnectorTransferHandler.getCtx().writeAndFlush(statusMsg(userStatus));

        //remove the connection
        clientConnContext.removeConn(ctx);
    }

    public void forceOffline(String userId) {
        ClientConn conn = clientConnContext.getConnByUserId(userId);
        clientConnContext.removeConn(conn.getNetId());
    }

    private Internal.InternalMsg statusMsg(UserStatus userStatus) throws JsonProcessingException {
        return Internal.InternalMsg.newBuilder()
            .setId(IdWorker.genId())
            .setVersion(1)
            .setFrom(Internal.InternalMsg.Module.CONNECTOR)
            .setDest(Internal.InternalMsg.Module.TRANSFER)
            .setCreateTime(System.currentTimeMillis())
            .setMsgType(Internal.InternalMsg.MsgType.USER_STATUS)
            .setMsgBody(objectMapper.writeValueAsString(userStatus))
            .build();
    }
}
