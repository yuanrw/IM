package com.yrw.im.common.domain;

import java.util.Date;

/**
 * Date: 2019-05-05
 * Time: 09:47
 *
 * @author yrw
 */
public class Offline extends DbModel {

    private Long conversationId;

    private String content;

    private Date gmtMsgCreate;

    public Date getGmtMsgCreate() {
        return gmtMsgCreate;
    }

    public void setGmtMsgCreate(Date gmtMsgCreate) {
        this.gmtMsgCreate = gmtMsgCreate;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
