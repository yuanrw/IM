package com.yrw.im.rest.web;

import com.yrw.im.common.exception.ImException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * Date: 2019-02-11
 * Time: 12:09
 *
 * @author yrw
 */
@ComponentScan(basePackages = {"com.yrw.im.rest"})
@PropertySource(value = {"file:${config}"})
@SpringBootApplication
public class RestStarter {
    public static void main(String[] args) {
        System.setProperty("log.path", (String) getProperties().get("log.path"));
        SpringApplication.run(RestStarter.class, args);
    }

    private static Properties getProperties() {
        try {
            InputStream inputStream;
            String path = System.getProperty("config");
            if (path == null) {
                throw new ImException("rest.properties is not defined");
            } else {
                inputStream = new FileInputStream(path);
                Properties properties = new Properties();
                properties.load(inputStream);
                return properties;
            }
        } catch (IOException e) {
            throw new ImException(e);
        }
    }

    @Bean
    public Properties properties() {
        return RestStarter.getProperties();
    }
}
