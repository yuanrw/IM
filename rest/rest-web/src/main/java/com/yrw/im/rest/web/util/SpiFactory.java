package com.yrw.im.rest.web.util;

import com.yrw.im.common.exception.ImException;
import com.yrw.im.rest.spi.UserSpi;
import com.yrw.im.rest.spi.domain.UserBase;
import com.yrw.im.rest.web.spi.impl.DefaultUserSpiImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Properties;

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
    private Properties properties;

    public SpiFactory(Properties properties) {
        this.properties = properties;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public UserSpi<? extends UserBase> getUserSpi() {
        String className = properties.getProperty("spi.user.impl.class");
        if (className == null) {
            return applicationContext.getBean(DefaultUserSpiImpl.class);
        }
        try {
            if (userSpi == null) {
                Class<?> userSpiImplClass = Class.forName(className);
                userSpi = (UserSpi<? extends UserBase>) applicationContext.getBean(userSpiImplClass);
            }
            return userSpi;
        } catch (ClassNotFoundException e) {
            throw new ImException("can not find class: " + className);
        }
    }
}