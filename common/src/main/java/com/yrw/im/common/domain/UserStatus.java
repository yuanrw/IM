package com.yrw.im.common.domain;

/**
 * Date: 2019-05-11
 * Time: 00:16
 *
 * @author yrw
 */
public class UserStatus {

    private String userId;

    private int status;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
