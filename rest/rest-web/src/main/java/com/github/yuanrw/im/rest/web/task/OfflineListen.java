package com.github.yuanrw.im.rest.web.task;

import com.github.yuanrw.im.common.domain.constant.ImConstant;
import com.github.yuanrw.im.common.parse.ParseService;
import com.github.yuanrw.im.protobuf.constant.MsgTypeEnum;
import com.github.yuanrw.im.protobuf.generate.Ack;
import com.github.yuanrw.im.protobuf.generate.Chat;
import com.github.yuanrw.im.rest.web.service.OfflineService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
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

    private ParseService parseService;
    private OfflineService offlineService;

    public OfflineListen(OfflineService offlineService) {
        this.parseService = new ParseService();
        this.offlineService = offlineService;
    }

    @PostConstruct
    public void init() {
        logger.info("[OfflineConsumer] Start listening Offline queue......");
    }

    @Override
    @RabbitHandler
    @RabbitListener(queues = ImConstant.MQ_OFFLINE_QUEUE, containerFactory = "listenerFactory")
    public void onMessage(Message message, Channel channel) throws Exception {
        logger.info("[OfflineConsumer] getUserSpi msg: {}", message.toString());
        try {
            int code = message.getBody()[0];

            byte[] msgBody = new byte[message.getBody().length - 1];
            System.arraycopy(message.getBody(), 1, msgBody, 0, message.getBody().length - 1);

            com.google.protobuf.Message msg = parseService.getMsgByCode(code, msgBody);
            if (code == MsgTypeEnum.CHAT.getCode()) {
                offlineService.saveChat((Chat.ChatMsg) msg);
            } else {
                offlineService.saveAck((Ack.AckMsg) msg);
            }

        } catch (Exception e) {
            logger.error("[OfflineConsumer] has error", e);
        } finally {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }
}
