package com.yrw.im.rest.session;

import com.yrw.im.common.util.SessionIdGenerator;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * Date: 2019-02-09
 * Time: 15:20
 *
 * @author yrw
 */
public class RedisSession implements Session {

    private static final String SESSION_KEY = "IM:SESSION:";
    private static RedisTemplate<String, String> template;

    private String id;

    private Long userId;

    private RedisSession(String id) {
        this.id = id;
    }

    private RedisSession(Long userId) {
        this.id = SessionIdGenerator.generateId();
        this.userId = userId;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public Long getUserId() {
        return userId;
    }

    @Override
    public Void setTimeout(long var1) {
        return null;
    }

    @Override
    public Void expire() {
        template.delete(SESSION_KEY + id);
        return null;
    }

    @Override
    public Object getAttribute(Object key) {
        return null;
    }

    @Override
    public Void setAttribute(Object key, Object value) {
        return null;
    }

    @Override
    public Object removeAttribute(Object key) {
        return null;
    }

    public static RedisSession createSession(Long userId) {
        RedisSession session = new RedisSession(userId);
        template.opsForValue().set(SESSION_KEY + session.getId(), String.valueOf(userId));
        template.expire(SESSION_KEY + session.getId(), 7, TimeUnit.DAYS);
        return session;
    }

    public static RedisSession getById(String id) {
        String userId = template.opsForValue().get(SESSION_KEY + id);
        if (userId == null) {
            return null;
        }
        RedisSession session = new RedisSession(id);
        session.userId = Long.parseLong(userId);
        return session;
    }

    public static void setTemplate(RedisTemplate<String, String> template) {
        RedisSession.template = template;
    }
}
