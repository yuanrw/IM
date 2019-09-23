package com.github.yuanrw.im.common.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Module to generate id, some id generator
 * such like snowFlake is depend on local machine time,
 * so it's better to deploy this module independently.
 * Date: 2019-05-06
 * Time: 20:09
 *
 * @author yrw
 */
public class IdWorker {

    private static SnowFlake snowFlake;
    private static ConcurrentMap<String, AtomicLong> sessionMap;

    static {
        snowFlake = new SnowFlake(1, 1);
        sessionMap = new ConcurrentHashMap<>();
    }

    /**
     * snowFlake,
     *
     * @return
     */
    public static Long genId() {
        return snowFlake.nextId();
    }

    /**
     * consistent id
     *
     * @return
     */
    public static Long nextId(String connectorId) {
        return sessionMap.computeIfAbsent(connectorId,
            key -> new AtomicLong(0)).incrementAndGet();
    }
}
