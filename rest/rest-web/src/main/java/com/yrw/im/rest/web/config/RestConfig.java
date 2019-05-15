package com.yrw.im.rest.web.config;

import com.yrw.im.common.domain.constant.MqConstant;
import com.yrw.im.rest.web.handler.ValidHandler;
import com.yrw.im.rest.web.session.RedisSession;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
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

    @Bean
    public SimpleRabbitListenerContainerFactory listenerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        return factory;
    }

    @Bean
    public Queue offlineQueue() {
        return new Queue(MqConstant.OFFLINE_QUEUE);
    }
}
