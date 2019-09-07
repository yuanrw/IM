package com.github.yuanrw.im.rest.web.vo;

import javax.validation.constraints.NotEmpty;

/**
 * Date: 2019-04-21
 * Time: 14:43
 *
 * @author yrw
 */
public class UserReq {

    @NotEmpty
    //    @Length(min = 6, max = 30)
    private String username;

    @NotEmpty
    private String pwd;

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
