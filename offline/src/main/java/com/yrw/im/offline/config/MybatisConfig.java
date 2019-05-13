package com.yrw.im.offline.config;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.yrw.im.common.domain.po.Offline;
import com.yrw.im.offline.mapper.OfflineMapper;
import org.apache.ibatis.datasource.pooled.PooledDataSourceFactory;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Date: 2019-05-09
 * Time: 14:57
 *
 * @author yrw
 */
public class MybatisConfig {

    private static SqlSessionFactory sqlSessionFactory;

    public static void setMybatisConfig() {
        GlobalConfig globalConfig = GlobalConfigUtils.defaults();

        GlobalConfig.DbConfig dbConfig = new GlobalConfig.DbConfig();
        dbConfig.setTablePrefix("im_");
        globalConfig.setDbConfig(dbConfig);

        MybatisConfiguration configuration = new MybatisConfiguration();

        GlobalConfigUtils.setGlobalConfig(configuration, globalConfig);

        configuration.init(globalConfig);

        configuration.setLazyLoadingEnabled(true);
        configuration.getTypeAliasRegistry().registerAlias(Offline.class);
        configuration.addMapper(OfflineMapper.class);

        PooledDataSourceFactory dsFactory = new PooledDataSourceFactory();
        Properties properties = new Properties();
        properties.setProperty("driver", "com.mysql.cj.jdbc.Driver");
        properties.setProperty("url", "jdbc:mysql://127.0.0.1/im");
        properties.setProperty("username", "root");
        properties.setProperty("password", "123456");

        JdbcTransactionFactory txFactory = new JdbcTransactionFactory();

        dsFactory.setProperties(properties);

        DataSource dataSource = dsFactory.getDataSource();

        Environment.Builder environmentBuilder = (new Environment.Builder("1")).transactionFactory(txFactory).dataSource(dataSource);
        configuration.setEnvironment(environmentBuilder.build());

        sqlSessionFactory = new MybatisSqlSessionFactoryBuilder().build(configuration);
    }

    public static SqlSessionFactory getSqlSessionFactory() {
        return sqlSessionFactory;
    }
}
