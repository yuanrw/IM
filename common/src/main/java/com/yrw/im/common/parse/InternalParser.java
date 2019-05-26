package com.yrw.im.common.parse;

import com.yrw.im.proto.generate.Internal;

/**
 * Date: 2019-05-26
 * Time: 20:36
 *
 * @author yrw
 */
public class InternalByEnumParser extends AbstractByEnumParser<Internal.InternalMsg.InternalMsgType, Internal.InternalMsg> {

    public InternalByEnumParser(int size) {
        super(size);
    }

    @Override
    protected Internal.InternalMsg.InternalMsgType getType(Internal.InternalMsg msg) {
        return msg.getMsgType();
    }
}
