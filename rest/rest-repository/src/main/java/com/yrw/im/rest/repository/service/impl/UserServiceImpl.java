package com.yrw.im.rest.repository.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yrw.im.common.domain.po.User;
import com.yrw.im.common.exception.ImException;
import com.yrw.im.rest.repository.mapper.UserMapper;
import com.yrw.im.rest.repository.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

/**
 * Date: 2019-04-07
 * Time: 18:36
 *
 * @author yrw
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public Long saveUser(String username, String pwdSha) {
        User user = new User();
        user.setUsername(username);
        user.setSalt(RandomStringUtils.randomAscii(16));
        user.setPwdHash(DigestUtils.sha256Hex(pwdSha + user.getSalt()));
        if (!save(user)) {
            throw new ImException("[rest] save user info failed");
        }
        return user.getId();
    }

    @Override
    public User verifyAndGet(String username, String pwdSha) {
        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            return null;
        }
        return verityPassword(pwdSha, user.getSalt(), user.getPwdHash()) ? user : null;
    }

    private boolean verityPassword(String pwdSha, String salt, String pwdHash) {
        String hashRes = DigestUtils.sha256Hex(pwdSha + salt);
        return hashRes.equals(pwdHash);
    }
}
