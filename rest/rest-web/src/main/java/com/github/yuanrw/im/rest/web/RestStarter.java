package com.github.yuanrw.im.rest.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Date: 2019-02-11
 * Time: 12:09
 *
 * @author yrw
 */
@EnableScheduling
@ComponentScan(basePackages = {"com.github.yuanrw.im.rest"})
@SpringBootApplication
public class RestStarter {
    public static void main(String[] args) {
        SpringApplication.run(RestStarter.class, args);
    }
}