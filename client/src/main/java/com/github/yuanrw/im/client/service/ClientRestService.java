package com.github.yuanrw.im.client.service;

import com.github.yuanrw.im.client.domain.UserReq;
import com.github.yuanrw.im.common.domain.UserInfo;
import com.github.yuanrw.im.common.domain.po.RelationDetail;
import com.github.yuanrw.im.common.rest.AbstractRestService;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import java.util.List;

/**
 * request for rest module
 * Date: 2019-04-21
 * Time: 16:45
 *
 * @author yrw
 */
public class ClientRestService extends AbstractRestService<ClientRestClient> {

    @Inject
    public ClientRestService(@Assisted String url) {
        super(ClientRestClient.class, url);
    }

    public UserInfo login(String username, String password) {
        return doRequest(() ->
            restClient.login(new UserReq(username, password)).execute());
    }

    public Void logout(String token) {
        return doRequest(() -> restClient.logout(token).execute());
    }

    public List<RelationDetail> friends(String userId, String token) {
        return doRequest(() -> restClient.friends(userId, token).execute());
    }

    public RelationDetail relation(String userId1, String userId2, String token) {
        return doRequest(() -> restClient.relation(userId1, userId2, token).execute());
    }
}
