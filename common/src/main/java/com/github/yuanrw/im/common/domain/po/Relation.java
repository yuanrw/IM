package com.github.yuanrw.im.common.domain.po;

/**
 * Date: 2019-02-09
 * Time: 20:44
 *
 * @author yrw
 */
public class Relation extends DbModel {

    private String userId1;

    private String userId2;

    private String encryptKey;

    public String getUserId1() {
        return userId1;
    }

    public void setUserId1(String userId1) {
        this.userId1 = userId1;
    }

    public String getUserId2() {
        return userId2;
    }

    public void setUserId2(String userId2) {
        this.userId2 = userId2;
    }

    public String getEncryptKey() {
        return encryptKey;
    }

    public void setEncryptKey(String encryptKey) {
        this.encryptKey = encryptKey;
    }
}
