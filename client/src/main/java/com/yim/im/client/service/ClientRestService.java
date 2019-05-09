package com.yim.im.client.service;

import com.yim.im.client.domain.UserReq;
import com.yrw.im.common.domain.Relation;
import com.yrw.im.common.domain.UserInfo;
import com.yrw.im.common.rest.AbstractRestService;
import io.netty.util.CharsetUtil;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.List;

/**
 * Date: 2019-04-21
 * Time: 16:45
 *
 * @author yrw
 */
public class RestService extends AbstractRestService<ClientRestClient> {

    public RestService() {
        super(ClientRestClient.class);
    }

    public UserInfo login(String username, String password) {
        return doRequest(() ->
            restClient.login(new UserReq(username, pwdSha256(password))).execute());
    }

    public Void logout(String token) {
        return doRequest(() -> restClient.logout(token).execute());
    }

    public List<Relation> friends(Long userId, String token) {
        return doRequest(() -> restClient.friends(userId, token).execute());
    }

    private String pwdSha256(String password) {
        return DigestUtils.sha256Hex(password.getBytes(CharsetUtil.UTF_8));
    }
}
