package com.santander.efx.config;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;

import redis.embedded.RedisServer;

@TestConfiguration
public class RedisTestConfiguration {
 
    private RedisServer redisServer;
 
    public RedisTestConfiguration(@Value("${spring.redis.port}") int redisPort) {
        this.redisServer = new RedisServer(redisPort);
    }
 
    @PostConstruct
    public void postConstruct() {
        redisServer.start();
    }
 
    @PreDestroy
    public void preDestroy() {
        redisServer.stop();
    }
}