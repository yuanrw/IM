package com.yrw.im.common.domain;

/**
 * Date: 2019-04-21
 * Time: 16:57
 *
 * @author yrw
 */
public class UserInfo {

    private Long userId;

    private String token;

    public UserInfo() {
    }

    public UserInfo(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
