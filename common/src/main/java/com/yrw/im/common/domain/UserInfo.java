package com.yrw.im.common.domain;

import com.yrw.im.common.domain.po.Relation;

import java.util.List;

/**
 * Date: 2019-04-21
 * Time: 16:57
 *
 * @author yrw
 */
public class UserInfo {

    private String id;

    private String token;

    private List<Relation> relations;

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

    public List<Relation> getRelations() {
        return relations;
    }

    public void setRelations(List<Relation> relations) {
        this.relations = relations;
    }
}
