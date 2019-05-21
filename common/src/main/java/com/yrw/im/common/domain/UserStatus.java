package com.yrw.im.common.domain;

/**
 * Date: 2019-05-11
 * Time: 00:16
 *
 * @author yrw
 */
public class UserStatus {

    private Long userId;

    private int status;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
