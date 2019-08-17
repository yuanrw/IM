package com.github.yuanrw.im.client.domain;

/**
 * Date: 2019-05-24
 * Time: 15:08
 *
 * @author yrw
 */
public class Friend {

    private String userId;

    private String username;

    private String encryptKey;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEncryptKey() {
        return encryptKey;
    }

    public void setEncryptKey(String encryptKey) {
        this.encryptKey = encryptKey;
    }
}
