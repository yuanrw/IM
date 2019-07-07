package com.yrw.im.rest.spi;

import com.yrw.im.rest.spi.domain.UserBase;
import reactor.core.publisher.Mono;

/**
 * Date: 2019-07-03
 * Time: 17:43
 *
 * @author yrw
 */
public interface UserSpi<T extends UserBase> {

    /**
     * get user by username and password, return user(id can not be null)
     * if username and password are right, else return Mono.empty().
     * <p>
     * be sure that your password has been properly encoded
     *
     * @param username
     * @param pwd
     * @return
     */
    Mono<T> getUser(String username, String pwd);

    /**
     * get user by id, if id not exist then return null.
     *
     * @param id
     * @return
     */
    Mono<T> getById(String id);
}