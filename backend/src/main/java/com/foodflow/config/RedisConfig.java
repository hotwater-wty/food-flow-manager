package com.foodflow.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

@Slf4j
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        log.info("开始创建Redis模板对象并序列化...");
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // 设置redis的连接工厂对象，即如何连接到redis服务器
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 设置redis的序列化器，如不指定则默认使用jdk自带的序列化器，存储到redis中的数据是二进制的，不方便查看和调试，且占用空间较大
        // value和hash value使用json序列化器，key和hash key使用字符串序列化器
        GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer();

        // 设置redis key的序列化器
        redisTemplate.setKeySerializer(RedisSerializer.string());
        // 设置redis hash key的序列化器
        redisTemplate.setHashKeySerializer(RedisSerializer.string());

        // 设置redis value的序列化器
        redisTemplate.setValueSerializer(jsonRedisSerializer);
        // 设置redis hash value的序列化器
        redisTemplate.setHashValueSerializer(jsonRedisSerializer);

        // 初始化redis模板对象
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }
}