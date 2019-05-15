package com.yrw.im.rest.web.session;

import java.io.Serializable;

/**
 * Date: 2019-02-09
 * Time: 15:14
 *
 * @author yrw
 */
public interface Session {

    Serializable getId();

    Long getUserId();

    Void setTimeout(long var1);

    Void expire();

    Object getAttribute(Object key);

    Void setAttribute(Object key, Object value);

    Object removeAttribute(Object key);
}
