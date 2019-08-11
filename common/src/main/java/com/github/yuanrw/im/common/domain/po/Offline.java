package com.github.yuanrw.im.common.domain.po;

/**
 * Date: 2019-05-05
 * Time: 09:47
 *
 * @author yrw
 */
public class Offline extends DbModel {

    private Long msgId;

    private Integer msgCode;

    private String toUserId;

    private byte[] content;

    public Long getMsgId() {
        return msgId;
    }

    public void setMsgId(Long msgId) {
        this.msgId = msgId;
    }

    public Integer getMsgCode() {
        return msgCode;
    }

    public void setMsgCode(Integer msgCode) {
        this.msgCode = msgCode;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
