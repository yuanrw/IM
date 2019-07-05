package com.yrw.im.rest.web.spi.impl;

import com.yrw.im.common.domain.po.User;
import com.yrw.im.rest.spi.UserSpi;
import com.yrw.im.rest.web.service.UserService;
import org.springframework.stereotype.Service;

/**
 * Date: 2019-07-03
 * Time: 17:49
 *
 * @author yrw
 */
@Service
public class DefaultUserSpi implements UserSpi<User> {

    private UserService userService;

    public DefaultUserSpi(UserService userService) {
        this.userService = userService;
    }

    @Override
    public User getUser(String username, String pwd) {
        return userService.verifyAndGet(username, pwd);
    }

    @Override
    public User getById(String id) {
        return userService.getById(Long.parseLong(id));
    }
}
