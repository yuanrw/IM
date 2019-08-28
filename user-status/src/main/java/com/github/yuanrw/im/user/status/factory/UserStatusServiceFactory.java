package com.github.yuanrw.im.user.status.factory;

import com.github.yuanrw.im.user.status.service.UserStatusService;

import java.util.Properties;

/**
 * Date: 2019-06-09
 * Time: 15:51
 *
 * @author yrw
 */
public interface UserStatusServiceFactory {

    /**
     * create a userStatusService
     *
     * @param properties
     * @return
     */
    UserStatusService createService(Properties properties);
}
