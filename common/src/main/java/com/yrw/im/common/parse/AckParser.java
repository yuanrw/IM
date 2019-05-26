package com.yrw.im.common.parse;

import com.yrw.im.proto.generate.Ack;

/**
 * Date: 2019-05-26
 * Time: 20:37
 *
 * @author yrw
 */
public class AckByEnumParser extends AbstractByEnumParser<Ack.AckMsg.MsgType, Ack.AckMsg> {
    @Override
    protected Ack.AckMsg.MsgType getType(Ack.AckMsg msg) {
        return null;
    }
}
