package com.yrw.im.rest.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yrw.im.common.domain.po.User;
import reactor.core.publisher.Mono;

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
    Mono<User> verifyAndGet(String username, String pwd);

    /**
     * 保存用户
     *
     * @param username 用户名
     * @param pwd      密码
     * @return
     */
    Mono<Long> saveUser(String username, String pwd);
}
