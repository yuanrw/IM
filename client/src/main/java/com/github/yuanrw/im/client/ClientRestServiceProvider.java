package com.github.yuanrw.im.client;

import com.google.inject.Provider;
import com.github.yuanrw.im.client.service.ClientRestService;

/**
 * Date: 2019-08-08
 * Time: 17:02
 *
 * @author yrw
 */
public class ClientRestServiceProvider implements Provider<ClientRestService> {

    public static String REST_URL;

    @Override
    public ClientRestService get() {
        return new ClientRestService(REST_URL);
    }
}
