package com.yrw.im.rest.web.vo;

import javax.validation.constraints.NotNull;

/**
 * Date: 2019-06-23
 * Time: 21:04
 *
 * @author yrw
 */
public class RelationReq {

    @NotNull
    private Long userId1;

    @NotNull
    private Long userId2;

    public Long getUserId1() {
        return userId1;
    }

    public void setUserId1(Long userId1) {
        this.userId1 = userId1;
    }

    public Long getUserId2() {
        return userId2;
    }

    public void setUserId2(Long userId2) {
        this.userId2 = userId2;
    }
}
