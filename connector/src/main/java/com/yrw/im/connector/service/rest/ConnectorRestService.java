package com.yrw.im.connector.service.rest;

import com.yrw.im.common.domain.po.Offline;
import com.yrw.im.common.rest.AbstractRestService;

import java.util.List;

/**
 * Date: 2019-05-28
 * Time: 00:18
 *
 * @author yrw
 */
public class ConnectorRestService extends AbstractRestService<ConnectorRestClient> {

    public ConnectorRestService() {
        super(ConnectorRestClient.class);
    }

    public List<Offline> offlines(String userId) {
        return doRequest(() -> restClient.pollOfflineMsg(userId).execute());
    }
}
