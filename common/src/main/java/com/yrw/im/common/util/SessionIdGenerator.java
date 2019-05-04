package com.yrw.im.common.util;

import java.util.UUID;

/**
 * Date: 2019-02-09
 * Time: 15:34
 *
 * @author yrw
 */
public class SessionIdGenerator {

    public static String generateId() {
        return UUID.randomUUID().toString();
    }
}
