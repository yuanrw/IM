package com.yrw.im.rest.web.consumer;

import com.rabbitmq.client.Channel;
import com.yrw.im.common.domain.constant.MqConstant;
import com.yrw.im.proto.generate.Chat;
import com.yrw.im.rest.repository.service.OfflineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Date: 2019-05-15
 * Time: 22:58
 *
 * @author yrw
 */
@Component
public class OfflineListen implements ChannelAwareMessageListener {
    private Logger logger = LoggerFactory.getLogger(OfflineListen.class);

    private OfflineService offlineService;

    @PostConstruct
    public void init() {
        logger.info("[OfflineConsumer] Start listening Offline queue......");
    }

    @Autowired
    public OfflineListen(OfflineService offlineService) {
        this.offlineService = offlineService;
    }

    @Override
    @RabbitHandler
    @RabbitListener(queues = MqConstant.OFFLINE_QUEUE, containerFactory = "listenerFactory")
    public void onMessage(Message message, Channel channel) throws Exception {
        logger.info("[OfflineConsumer] get msg");
        try {
            Chat.ChatMsg chatMsg = Chat.ChatMsg.parseFrom(message.getBody());
            offlineService.saveChatMsg(chatMsg);
        } catch (Exception e) {
            logger.error("[OfflineConsumer] has error", e);
        } finally {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }
}
