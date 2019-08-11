package com.github.yuanrw.im.connector.config;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * Date: 2019-08-08
 * Time: 16:31
 *
 * @author yrw
 */
public class ConnectorModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().build(ConnectorRestServiceFactory.class));
    }
}
