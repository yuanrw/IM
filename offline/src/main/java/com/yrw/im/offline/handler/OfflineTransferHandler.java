package com.yrw.im.offline.handler;

import com.google.inject.Inject;
import com.google.protobuf.Message;
import com.yrw.im.offline.service.OfflineService;
import com.yrw.im.proto.generate.Internal;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Date: 2019-05-07
 * Time: 20:22
 *
 * @author yrw
 */
public class OfflineTransferHandler extends SimpleChannelInboundHandler<Message> {

    private OfflineService offlineService;

    @Inject
    public OfflineTransferHandler(OfflineService offlineService) {
        this.offlineService = offlineService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        if (msg instanceof Internal.InternalMsg) {
            Internal.InternalMsg m = (Internal.InternalMsg) msg;
            if (m.getMsgType() == Internal.InternalMsg.InternalMsgType.OFFLINE_MSG) {
                Internal.InternalMsg offlineMsg = offlineService.listOfflineMsg(Long.parseLong(m.getMsgBody()));
                ctx.writeAndFlush(offlineMsg);
            }
        }
    }
}
