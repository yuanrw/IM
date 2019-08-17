package com.github.yuanrw.im.transfer.config;

import com.github.yuanrw.im.user.status.factory.UserStatusServiceFactory;
import com.github.yuanrw.im.user.status.service.UserStatusService;
import com.github.yuanrw.im.user.status.service.impl.RedisUserStatusServiceImpl;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * Date: 2019-06-09
 * Time: 15:52
 *
 * @author yrw
 */
public class TransferModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new FactoryModuleBuilder()
            .implement(UserStatusService.class, RedisUserStatusServiceImpl.class)
            .build(UserStatusServiceFactory.class));
    }
}