package com.yrw.im.common.domain.po;

/**
 * Date: 2019-05-05
 * Time: 09:47
 *
 * @author yrw
 */
public class Offline extends DbModel {

    private Long fromUserId;

    private Long toUserId;

    private String content;

    public Long getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(Long fromUserId) {
        this.fromUserId = fromUserId;
    }

    public Long getToUserId() {
        return toUserId;
    }

    public void setToUserId(Long toUserId) {
        this.toUserId = toUserId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
