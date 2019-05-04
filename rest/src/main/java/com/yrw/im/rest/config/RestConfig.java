package com.yrw.im.rest.config;

import com.yrw.im.rest.handler.ValidHandler;
import com.yrw.im.rest.session.RedisSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import javax.validation.Validator;

/**
 * Date: 2019-04-21
 * Time: 15:08
 *
 * @author yrw
 */
@Configuration
public class RestConfig {

    @Bean
    public Integer init(Validator validator, RedisTemplate<String, String> redisTemplate) {
        ValidHandler.setValidator(validator);
        RedisSession.setTemplate(redisTemplate);
        return 1;
    }
}
