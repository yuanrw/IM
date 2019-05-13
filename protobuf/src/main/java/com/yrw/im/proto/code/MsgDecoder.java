package com.yrw.im.proto.code;

import com.google.inject.Inject;
import com.google.protobuf.Message;
import com.yrw.im.proto.constant.MsgTypeEnum;
import com.yrw.im.proto.parse.ParseService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Date: 2019-04-14
 * Time: 15:06
 *
 * @author yrw
 */
public class MsgDecoder extends ByteToMessageDecoder {
    private static final Logger logger = LoggerFactory.getLogger(MsgDecoder.class);

    private ParseService parseService;

    @Inject
    public MsgDecoder() {
        this.parseService = new ParseService();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        in.markReaderIndex();

        if (in.readableBytes() < 4) {
            in.resetReaderIndex();
            return;
        }

        int length = in.readInt();

        if (length < 0) {
            ctx.close();
            logger.error("[IM msg decoder]message length less than 0, channel closed");
            return;
        }

        if (length > in.readableBytes() - 4) {
            //注意！编解码器加这种in.readInt()日志，在大并发的情况下很可能会抛数组越界异常！
            //logger.error("message received is incomplete,ptoNum:{}, length:{}, readable:{}", in.readInt(), length, in.readableBytes());
            in.resetReaderIndex();
            return;
        }

        int code = in.readInt();
        ByteBuf byteBuf = Unpooled.buffer(length);

        in.readBytes(byteBuf);

        byte[] body = byteBuf.array();

        Message msg = parseService.getMsg(MsgTypeEnum.getByCode(code), body);
        out.add(msg);

        logger.info("[IM msg decoder]received message: content length {}, msgTypeCode: {}", length, code);
    }
}
