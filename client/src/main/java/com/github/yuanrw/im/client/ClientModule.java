package com.github.yuanrw.im.client;

import com.github.yuanrw.im.client.context.RelationCache;
import com.github.yuanrw.im.client.context.impl.MemoryRelationCache;
import com.github.yuanrw.im.client.service.ClientRestService;
import com.google.inject.AbstractModule;

/**
 * Date: 2019-07-03
 * Time: 16:43
 *
 * @author yrw
 */
public class ClientModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(RelationCache.class).to(MemoryRelationCache.class);
        bind(ClientRestService.class).toProvider(ClientRestServiceProvider.class);
    }
}
