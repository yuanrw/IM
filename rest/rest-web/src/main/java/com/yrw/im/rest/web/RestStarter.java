package com.yrw.im.rest.web;

import com.yrw.im.common.exception.ImException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;

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
    public static void main(String[] args) throws IOException {
        System.setProperty("log.path", (String) getProperties().get("log.path"));
        SpringApplication.run(RestStarter.class, args);
    }

    private static Properties getProperties() {
        InputStream inputStream;
        String path = System.getProperty("config");
        if (path == null) {
            throw new ImException("rest.properties is not defined");
        } else {
            try {
                inputStream = new FileInputStream(path);
            } catch (IOException e) {
                try {
                    inputStream = new ClassPathResource("rest.properties").getInputStream();
                } catch (IOException e1) {
                    throw new ImException(e1);
                }
            }
            Properties properties = new Properties();
            try {
                properties.load(inputStream);
                return properties;
            } catch (IOException e) {
                throw new ImException(e);
            }
        }
    }

    @Bean
    public Properties properties() {
        return RestStarter.getProperties();
    }
}
