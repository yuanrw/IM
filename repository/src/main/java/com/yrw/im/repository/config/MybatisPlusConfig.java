package com.yrw.im.repository.config;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.yrw.im.common.domain.po.DbModel;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Date: 2019-02-11
 * Time: 22:26
 *
 * @author yrw
 */
@Configuration
@MapperScan(value = "com.yrw.im.repository.mapper")
@ComponentScan(basePackages = "com.yrw.im.repository.service")
public class MybatisPlusConfig {

    @Bean
    @Primary
    public MybatisPlusProperties mybatisPlusProperties() {
        MybatisPlusProperties properties = new MybatisPlusProperties();
        GlobalConfig globalConfig = new GlobalConfig();

        properties.setTypeAliasesSuperType(DbModel.class);
        properties.setMapperLocations(new String[]{"classpath*:/mapper/**/*.xml"});
        properties.setGlobalConfig(globalConfig);

        GlobalConfig.DbConfig dbConfig = new GlobalConfig.DbConfig();
        dbConfig.setTablePrefix("im_");
        globalConfig.setDbConfig(dbConfig);

        return properties;
    }
}
