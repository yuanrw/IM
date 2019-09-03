package com.github.yuanrw.im.connector.config;

/**
 * Date: 2019-06-14
 * Time: 18:29
 *
 * @author yrw
 */
public class ConnectorConfig {

    private Integer port;
    private String[] transferUrls;
    private String restUrl;
    private String redisHost;
    private Integer redisPort;
    private String redisPassword;

    public String getRedisHost() {
        return redisHost;
    }

    public void setRedisHost(String redisHost) {
        this.redisHost = redisHost;
    }

    public Integer getRedisPort() {
        return redisPort;
    }

    public void setRedisPort(Integer redisPort) {
        this.redisPort = redisPort;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public void setRedisPassword(String redisPassword) {
        this.redisPassword = redisPassword;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String[] getTransferUrls() {
        return transferUrls;
    }

    public void setTransferUrls(String[] transferUrls) {
        this.transferUrls = transferUrls;
    }

    public String getRestUrl() {
        return restUrl;
    }

    public void setRestUrl(String restUrl) {
        this.restUrl = restUrl;
    }
}