package com.github.yuanrw.im.rest.web.spi.impl;

import com.github.yuanrw.im.common.domain.po.User;
import com.github.yuanrw.im.rest.spi.UserSpi;
import com.github.yuanrw.im.rest.spi.domain.UserBase;
import com.github.yuanrw.im.rest.web.service.UserService;
import org.springframework.stereotype.Service;

/**
 * Date: 2019-07-03
 * Time: 17:49
 *
 * @author yrw
 */
@Service
public class DefaultUserSpiImpl implements UserSpi<UserBase> {

    private UserService userService;

    public DefaultUserSpiImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserBase getUser(String username, String pwd) {
        User user = userService.verifyAndGet(username, pwd);
        if (user == null) {
            return null;
        }

        UserBase userBase = new UserBase();
        userBase.setId(user.getId() + "");
        userBase.setUsername(user.getUsername());
        return userBase;
    }

    @Override
    public UserBase getById(String id) {
        User user = userService.getById(Long.parseLong(id));
        if (user == null) {
            return null;
        }

        UserBase userBase = new UserBase();
        userBase.setId(userBase.getId());
        userBase.setUsername(userBase.getUsername());
        return userBase;
    }
}
