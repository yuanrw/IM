package com.github.yuanrw.im.common.domain.constant;

import java.util.stream.Stream;

/**
 * Date: 2019-09-07
 * Time: 07:42
 *
 * @author yrw
 */
public enum MsgVersion {

    /**
     * version 1
     */
    V1(1);

    private int version;

    MsgVersion(int version) {
        this.version = version;
    }

    public static MsgVersion get(int version) {
        return Stream.of(values()).filter(n -> n.version == version)
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    public int getVersion() {
        return version;
    }
}