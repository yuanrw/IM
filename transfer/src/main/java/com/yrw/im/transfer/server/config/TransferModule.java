package com.yrw.im.transfer.server.config;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.yrw.im.transfer.user.status.factory.UserStatusServiceFactory;
import com.yrw.im.transfer.user.status.service.UserStatusService;
import com.yrw.im.transfer.user.status.service.impl.RedisUserStatusServiceImpl;

/**
 * Date: 2019-06-09
 * Time: 15:52
 *
 * @author yrw
 */
public class TransferModule implements Module {

    @Override
    public void configure(Binder binder) {
        binder.install(new FactoryModuleBuilder()
            .implement(UserStatusService.class, RedisUserStatusServiceImpl.class)
            .build(UserStatusServiceFactory.class));
    }
}
