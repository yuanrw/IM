package com.github.yuanrw.im.rest.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.yuanrw.im.common.domain.po.Offline;

/**
 * Date: 2019-05-05
 * Time: 09:46
 *
 * @author yrw
 */
public interface OfflineMapper extends BaseMapper<Offline> {

    /**
     * read offline msg from db, cas
     *
     * @param msgId
     * @return
     */
    int readMsg(Long msgId);
}
