package com.yrw.im.connector.service;

import com.google.inject.Inject;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.yrw.im.common.domain.po.Offline;
import com.yrw.im.common.parse.ParseService;
import com.yrw.im.connector.config.ConnectorRestServiceFactory;
import com.yrw.im.connector.service.rest.ConnectorRestService;
import com.yrw.im.connector.start.ConnectorStarter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Date: 2019-05-28
 * Time: 00:24
 *
 * @author yrw
 */
public class OfflineService {

    private ConnectorRestService connectorRestService;
    private ParseService parseService;

    @Inject
    public OfflineService(ConnectorRestServiceFactory connectorRestServiceFactory, ParseService parseService) {
        this.connectorRestService = connectorRestServiceFactory.createService(ConnectorStarter.CONNECTOR_CONFIG.getRestUrl());
        this.parseService = parseService;
    }

    public List<Message> pollOfflineMsg(String userId) {
        List<Offline> msgs = connectorRestService.offlines(userId);
        return msgs.stream()
            .map(o -> {
                try {
                    return parseService.getMsgByCode(o.getMsgCode(), o.getContent());
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
