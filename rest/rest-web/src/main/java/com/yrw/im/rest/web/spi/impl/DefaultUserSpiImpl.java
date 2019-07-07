package com.yrw.im.rest.web.spi.impl;

import com.yrw.im.rest.spi.UserSpi;
import com.yrw.im.rest.spi.domain.UserBase;
import com.yrw.im.rest.web.service.UserService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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
    public Mono<UserBase> getUser(String username, String pwd) {
        return Mono.fromSupplier(() -> userService.verifyAndGet(username, pwd))
            .map(u -> {
                UserBase userBase = new UserBase();
                userBase.setId(u.getId() + "");
                userBase.setUsername(u.getUsername());
                return userBase;
            });
    }

    @Override
    public Mono<UserBase> getById(String id) {
        return Mono.fromSupplier(() -> userService.getById(Long.parseLong(id)))
            .map(r -> {
                UserBase userBase = new UserBase();
                userBase.setId(userBase.getId());
                userBase.setUsername(userBase.getUsername());
                return userBase;
            });
    }
}
