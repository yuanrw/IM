package com.yim.im.client.domain;

/**
 * Date: 2019-04-21
 * Time: 14:43
 *
 * @author yrw
 */
public class UserReq {
    private String username;
    private String pwdSha;

    public UserReq(String username, String pwdSha) {
        this.username = username;
        this.pwdSha = pwdSha;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPwdSha() {
        return pwdSha;
    }

    public void setPwdSha(String pwdSha) {
        this.pwdSha = pwdSha;
    }
}
