package com.yrw.im.proto.constant;

import java.util.stream.Stream;

/**
 * 用户登录状态
 * Date: 2019-04-25
 * Time: 22:26
 *
 * @author yrw
 */
public enum UserStatusEnum {

    /**
     * 下线
     */
    OFFLINE(0),

    /**
     * 上线
     */
    ONLINE(1);

    private int code;

    UserStatusEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static UserStatusEnum getStatus(int code) {
        return Stream.of(values()).filter(s -> s.code == code)
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
