package com.github.yuanrw.im.user.status.factory;

import com.github.yuanrw.im.user.status.service.UserStatusService;

/**
 * Date: 2019-06-09
 * Time: 15:51
 *
 * @author yrw
 */
public interface UserStatusServiceFactory {

    /**
     * create a userStatusService
     * //todo: need to be singleton
     *
     * @param host
     * @param port
     * @return
     */
    UserStatusService createService(String host, int port);
}
