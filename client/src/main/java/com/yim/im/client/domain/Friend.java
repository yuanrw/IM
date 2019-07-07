package com.yim.im.client.domain;

/**
 * Date: 2019-05-24
 * Time: 15:08
 *
 * @author yrw
 */
public class Friend {

    private String userId;

    private String encryptKey;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEncryptKey() {
        return encryptKey;
    }

    public void setEncryptKey(String encryptKey) {
        this.encryptKey = encryptKey;
    }
}
