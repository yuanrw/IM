package com.github.yuanrw.im.common.domain;

import com.github.yuanrw.im.common.domain.po.RelationDetail;

import java.util.List;

/**
 * Date: 2019-04-21
 * Time: 16:57
 *
 * @author yrw
 */
public class UserInfo {

    private String id;

    private String username;

    private String token;

    private List<RelationDetail> relations;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<RelationDetail> getRelations() {
        return relations;
    }

    public void setRelations(List<RelationDetail> relations) {
        this.relations = relations;
    }
}
