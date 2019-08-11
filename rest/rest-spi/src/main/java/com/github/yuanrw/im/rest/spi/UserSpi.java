package com.github.yuanrw.im.rest.spi;

import com.github.yuanrw.im.rest.spi.domain.UserBase;

/**
 * Date: 2019-07-03
 * Time: 17:43
 *
 * @author yrw
 */
public interface UserSpi<T extends UserBase> {

    /**
     * get user by username and password, return user(id can not be null)
     * if username and password are right, else return null.
     * <p>
     * be sure that your password has been properly encrypted
     *
     * @param username
     * @param pwd
     * @return
     */
    T getUser(String username, String pwd);

    /**
     * get user by id, if id not exist then return null.
     *
     * @param id
     * @return
     */
    T getById(String id);
}