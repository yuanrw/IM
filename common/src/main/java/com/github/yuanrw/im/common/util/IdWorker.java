package com.github.yuanrw.im.common.util;

/**
 * Date: 2019-05-06
 * Time: 20:09
 *
 * @author yrw
 */
public class IdWorker {

    private static SnowFlake snowFlake;

    static {
        snowFlake = new SnowFlake(1, 1);
    }

    public static Long genId() {
        return snowFlake.nextId();
    }
}
