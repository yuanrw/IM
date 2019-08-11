package com.github.yuanrw.im.common.parse;

import com.github.yuanrw.im.protobuf.generate.Internal;

/**
 * Date: 2019-05-26
 * Time: 20:36
 *
 * @author yrw
 */
public class InternalParser extends AbstractByEnumParser<Internal.InternalMsg.MsgType, Internal.InternalMsg> {

    public InternalParser(int size) {
        super(size);
    }

    @Override
    protected Internal.InternalMsg.MsgType getType(Internal.InternalMsg msg) {
        return msg.getMsgType();
    }
}
