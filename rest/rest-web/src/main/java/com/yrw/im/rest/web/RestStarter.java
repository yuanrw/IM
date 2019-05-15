package com.yrw.im.rest.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Date: 2019-02-11
 * Time: 12:09
 *
 * @author yrw
 */
@ComponentScan(basePackages = {"com.yrw.im.rest"})
@SpringBootApplication
public class RestStarter {
    public static void main(String[] args) {
        SpringApplication.run(RestStarter.class, args);
    }
}
