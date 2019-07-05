package com.yrw.im.rest.web.util;

import com.yrw.im.common.domain.po.DbModel;
import com.yrw.im.common.exception.ImException;
import com.yrw.im.rest.spi.UserSpi;
import com.yrw.im.rest.web.spi.impl.DefaultUserSpi;
import org.springframework.beans.factory.annotation.Autowired;
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

    private UserSpi<? extends DbModel> userSpi;
    private ApplicationContext applicationContext;

    @Autowired
    private Properties properties;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public UserSpi<? extends DbModel> getUserSpi() {
        String className = properties.getProperty("spi.user.impl.class");
        if (className == null) {
            return applicationContext.getBean(DefaultUserSpi.class);
        }
        try {
            if (userSpi == null) {
                Class<?> userSpiImplClass = Class.forName(className);
                userSpi = (UserSpi<? extends DbModel>) applicationContext.getBean(userSpiImplClass);
            }
            return userSpi;
        } catch (ClassNotFoundException e) {
            throw new ImException("can not find class: " + className);
        }
    }
}