package com.yrw.im.rest.domain.vo;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

/**
 * Date: 2019-04-21
 * Time: 14:43
 *
 * @author yrw
 */
public class UserReq {

    @NotEmpty
    @Length(min = 6, max = 30)
    private String username;

    @NotEmpty
    private String pwdSha;

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
