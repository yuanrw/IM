package com.yrw.im.offline.config;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.inject.AbstractModule;
import com.yrw.im.offline.service.OfflineService;
import com.yrw.im.offline.service.impl.OfflineServiceImpl;

/**
 * Date: 2019-05-07
 * Time: 19:59
 *
 * @author yrw
 */
public class OfflineModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(OfflineService.class).to(OfflineServiceImpl.class);
        bind(IService.class).to(ServiceImpl.class);
    }
}
