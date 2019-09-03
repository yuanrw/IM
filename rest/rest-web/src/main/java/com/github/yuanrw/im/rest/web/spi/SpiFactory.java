package com.github.yuanrw.im.rest.web.spi;

import com.github.yuanrw.im.common.exception.ImException;
import com.github.yuanrw.im.rest.spi.UserSpi;
import com.github.yuanrw.im.rest.spi.domain.UserBase;
import com.github.yuanrw.im.rest.web.spi.impl.DefaultUserSpiImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Date: 2019-07-03
 * Time: 17:50
 *
 * @author yrw
 */
@Component
public class SpiFactory implements ApplicationContextAware {

    private UserSpi<? extends UserBase> userSpi;
    private ApplicationContext applicationContext;

    @Value("${spi.user.impl.class}")
    private String userSpiImplClassName;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public UserSpi<? extends UserBase> getUserSpi() {
        if (StringUtils.isEmpty(userSpiImplClassName)) {
            return applicationContext.getBean(DefaultUserSpiImpl.class);
        }
        try {
            if (userSpi == null) {
                Class<?> userSpiImplClass = Class.forName(userSpiImplClassName);
                userSpi = (UserSpi<? extends UserBase>) applicationContext.getBean(userSpiImplClass);
            }
            return userSpi;
        } catch (ClassNotFoundException e) {
            throw new ImException("can not find class: " + userSpiImplClassName);
        }
    }
}