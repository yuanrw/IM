package com.yrw.im.status.domain;

import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Date: 2019-05-19
 * Time: 21:22
 *
 * @author yrw
 */
public class Connector {

    /**
     * connector id
     */
    private String id;

    /**
     * connector上在线的用户
     */
    private Set<Long> userSet;

    public Connector(String id) {
        this.id = id;
        this.userSet = Sets.newConcurrentHashSet();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void addUser(Long id) {
        this.userSet.add(id);
    }

    public void remUser(Long id) {
        this.userSet.remove(id);
    }

    public List<Long> getUsers() {
        return new ArrayList<>(userSet);
    }

    public boolean containUser(Long id) {
        return userSet.contains(id);
    }
}
