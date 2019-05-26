package com.yrw.im.common.exception;

/**
 * Date: 2019-02-09
 * Time: 13:53
 *
 * @author yrw
 */
public class ImException extends RuntimeException {

    public ImException(String message, Throwable e) {
        super(message, e);
    }

    public ImException(String errMsg) {
        super(errMsg);
    }
}
