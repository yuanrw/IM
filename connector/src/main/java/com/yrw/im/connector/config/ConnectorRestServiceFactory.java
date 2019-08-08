package com.yrw.im.connector.config;

import com.yrw.im.connector.service.rest.ConnectorRestService;

/**
 * Date: 2019-08-08
 * Time: 17:44
 *
 * @author yrw
 */
public interface ConnectorRestServiceFactory {

    /**
     * create a ConnectorRestService
     * //todo: need to be singleton
     *
     * @param url
     * @return
     */
    ConnectorRestService createService(String url);
}