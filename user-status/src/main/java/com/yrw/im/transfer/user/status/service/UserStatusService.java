package com.yrw.im.transfer.user.status.service;

/**
 * Date: 2019-06-09
 * Time: 15:55
 *
 * @author yrw
 */
public interface UserStatusService {

    /**
     * user online
     *
     * @param connectorId
     * @param userId
     * @return the user's previous connection id, if don't exist then return null
     */
    String online(String connectorId, Long userId);

    /**
     * user offline
     *
     * @param userId
     */
    void offline(Long userId);

    /**
     * get connector id by user id
     *
     * @param userId
     * @return
     */
    String getConnectorId(Long userId);
}
