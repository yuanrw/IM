package com.yrw.im.offline.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.yrw.im.common.domain.po.Offline;
import com.yrw.im.common.exception.ImException;
import com.yrw.im.offline.config.MybatisConfig;
import com.yrw.im.offline.mapper.OfflineMapper;
import com.yrw.im.offline.service.OfflineService;
import com.yrw.im.proto.generate.Chat;
import com.yrw.im.proto.generate.Internal;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Supplier;

/**
 * Date: 2019-05-05
 * Time: 09:49
 *
 * @author yrw
 */
public class OfflineServiceImpl extends ServiceImpl<OfflineMapper, Offline> implements OfflineService {
    private static Logger logger = LoggerFactory.getLogger(OfflineServiceImpl.class);

    private ObjectMapper objectMapper;

    @Inject
    public OfflineServiceImpl() {
        objectMapper = new ObjectMapper();
    }

    @Override
    public void saveChatMsg(Chat.ChatMsg msg) {

        withTransaction(() -> {
            Offline offline = new Offline();
            offline.setFromUserId(msg.getFromId());
            offline.setToUserId(msg.getDestId());
            offline.setContent(msg.toString());

            if (!save(offline)) {
                throw new ImException("");
            }
            return null;
        });
    }

    @Override
    public Internal.InternalMsg listOfflineMsg(Long userId) throws JsonProcessingException {
        List<Offline> offlineMsgList = list(new LambdaQueryWrapper<Offline>()
            .eq(Offline::getToUserId, userId));

        return Internal.InternalMsg.newBuilder()
            .setVersion(1)
            .setCreateTime(System.currentTimeMillis())
            .setFrom(Internal.InternalMsg.Module.TRANSFER)
            .setDest(Internal.InternalMsg.Module.CONNECTOR)
            .setMsgType(Internal.InternalMsg.InternalMsgType.OFFLINE_MSG)
            .setMsgBody(objectMapper.writeValueAsString(offlineMsgList))
            .build();
    }

    private void withTransaction(Supplier<Void> sqlSupplier) {
        SqlSession session = MybatisConfig.getSqlSessionFactory().openSession();
        this.baseMapper = session.getMapper(OfflineMapper.class);

        try {
            sqlSupplier.get();
            session.commit();
        } catch (Exception e) {
            logger.error("[OfflineService] has error", e);
            session.rollback();
            throw new ImException("");
        } finally {
            session.close();
        }
    }
}
