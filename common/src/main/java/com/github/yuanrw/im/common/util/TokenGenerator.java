package com.github.yuanrw.im.common.util;

import java.util.UUID;

/**
 * Date: 2019-02-09
 * Time: 15:34
 *
 * @author yrw
 */
public class TokenGenerator {

    public static String generate() {
        return UUID.randomUUID().toString();
    }
}
