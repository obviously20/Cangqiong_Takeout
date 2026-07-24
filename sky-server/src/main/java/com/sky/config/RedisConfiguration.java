package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfiguration {

    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory){
        log.info("开始创建Redis模板对象...");
        //创建RedisTemplate对象,用于操作Redis数据库
        RedisTemplate redisTemplate = new RedisTemplate();
        //设置RedisTemplate的连接工厂,用于连接Redis数据库
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        //设置RedisTemplate的序列化器,用于序列化和反序列化对象
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        return redisTemplate;

    }

}
