package com.yrw.im.offline.consumer;

import com.google.inject.Inject;
import com.rabbitmq.client.Delivery;
import com.yrw.im.offline.service.OfflineService;
import com.yrw.im.offline.start.OfflineStarter;
import com.yrw.im.proto.generate.Chat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Date: 2019-05-05
 * Time: 09:55
 *
 * @author yrw
 */
public class OfflineConsumer {
    private Logger logger = LoggerFactory.getLogger(OfflineConsumer.class);

    private OfflineService offlineService;

    @Inject
    public OfflineConsumer(OfflineService offlineService) {
        this.offlineService = offlineService;
    }

    public void doConsumer(String consumerTag, Delivery delivery) throws IOException {
        logger.info("[OfflineConsumer] get msg: ");
        try {
            Chat.ChatMsg chatMsg = Chat.ChatMsg.parseFrom(delivery.getBody());
            offlineService.saveChatMsg(chatMsg);
        } catch (Exception e) {
            logger.error("[OfflineConsumer] has error", e);
        } finally {
            OfflineStarter.getChannel().basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        }
    }
}
