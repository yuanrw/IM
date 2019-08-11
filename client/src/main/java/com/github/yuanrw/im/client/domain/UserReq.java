package com.github.yuanrw.im.client.domain;

/**
 * Date: 2019-04-21
 * Time: 14:43
 *
 * @author yrw
 */
public class UserReq {
    private String username;
    private String pwd;

    public UserReq(String username, String pwd) {
        this.username = username;
        this.pwd = pwd;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }
}
