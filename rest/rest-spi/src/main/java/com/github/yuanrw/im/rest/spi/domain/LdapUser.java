package com.github.yuanrw.im.rest.spi.domain;

/**
 * Date: 2019-07-07
 * Time: 17:08
 *
 * @author yrw
 */
public class LdapUser extends UserBase {

    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
