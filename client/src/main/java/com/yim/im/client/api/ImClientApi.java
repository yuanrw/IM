package com.yim.im.client.api;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Date: 2019-05-14
 * Time: 10:34
 *
 * @author yrw
 */
public class ImClientApi {

    private static Injector injector = Guice.createInjector();

    public static <T> T getApi(Class<T> clazz) {
        assert clazz == UserApi.class || clazz == ChatApi.class;
        return injector.getInstance(clazz);
    }
}
