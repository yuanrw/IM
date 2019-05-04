package com.yrw.im.common.exception;

/**
 * Date: 2019-02-09
 * Time: 13:53
 *
 * @author yrw
 */
public class ImException extends RuntimeException {

    private String errCode;
    private String errMsg;

    public ImException() {
    }

    public ImException(String errCode) {
        super(errCode);
        this.errCode = errCode;
    }

    public ImException(String errCode, String errMsg) {
        super(errCode + ":" + errMsg);
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }
}
