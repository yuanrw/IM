package com.github.yuanrw.im.rest.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.yuanrw.im.common.domain.po.User;

/**
 * Date: 2019-04-07
 * Time: 18:35
 *
 * @author yrw
 */
public interface UserService extends IService<User> {

    /**
     * 验证用户密码，成功则返回用户，失败返回null
     *
     * @param username 用户名
     * @param pwd      密码
     * @return
     */
    User verifyAndGet(String username, String pwd);
}
