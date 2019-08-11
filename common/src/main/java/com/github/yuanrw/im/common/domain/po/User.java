package com.github.yuanrw.im.common.domain.po;

/**
 * Date: 2019-02-09
 * Time: 14:02
 *
 * @author yrw
 */
public class User extends DbModel {

    private String username;

    private String pwdHash;

    private String salt;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPwdHash() {
        return pwdHash;
    }

    public void setPwdHash(String pwdHash) {
        this.pwdHash = pwdHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }
}
