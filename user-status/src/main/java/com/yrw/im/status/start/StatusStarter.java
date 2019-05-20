package com.yrw.im.status.start;

/**
 * Date: 2019-05-19
 * Time: 21:10
 *
 * @author yrw
 */
public class StatusStarter {

    public static void main(String[] args) {
        int port = 9084;
        StatusServer.start(port);
    }
}
