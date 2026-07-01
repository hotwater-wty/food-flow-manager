package com.foodflow.common.utils;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodflow.common.constant.CacheConstants;
import com.foodflow.common.exception.BusinessException;
import com.foodflow.common.result.CacheResult;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CacheUtil {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 从缓存中获取数据，简单类型
     * @param cacheKey 缓存键
     * @param type 缓存值类型
     * @return 缓存值
     */
    public <T> CacheResult<T> getCache(String cacheKey, Class<T> type) {
        // 从缓存中获取数据
        if (cacheKey == null || cacheKey.isEmpty()) {
            throw new BusinessException("缓存键不能为空");
        }
        String cachedJson = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cachedJson == null){
            return CacheResult.miss();
        }
        if (isEmptyCache(cachedJson)) {
            return CacheResult.empty();
        }
        try {
            return CacheResult.hit(objectMapper.readValue(
                    cachedJson, 
                    type));
            
        } catch (JsonProcessingException e) {
            throw new BusinessException("缓存解析失败");
        }
    }

    /**
     * 从缓存中获取数据，支持复杂类型
     * @param cacheKey 缓存键
     * @param typeReference 缓存值类型
     * @return 缓存值
     */
    public <T> CacheResult<T> getCache(String cacheKey, TypeReference<T> typeReference) {
        // 从缓存中获取数据
        if (cacheKey == null || cacheKey.isEmpty()) {
            throw new BusinessException("缓存键不能为空");
        }
        String cachedJson = stringRedisTemplate.opsForValue().get(cacheKey);
        
        if (cachedJson == null){
            return CacheResult.miss();
        }
        if (isEmptyCache(cachedJson)) {
            return CacheResult.empty();
        }
        try {
            return CacheResult.hit(objectMapper.readValue(
                    cachedJson, 
                    typeReference));
        } catch (JsonProcessingException e) {
            throw new BusinessException("缓存解析失败");
        }
    }

    /**
     * 缓存数据
     * @param cacheKey 缓存键
     * @param value 缓存值
     */
    public void setCache(String cacheKey, Object value) {
        // 缓存数据
        if (cacheKey == null || cacheKey.isEmpty()) {
            throw new BusinessException("缓存键不能为空");
        }
        if (value == null) {
            throw new BusinessException("缓存值不能为空");
        }
        try{
            String json = objectMapper.writeValueAsString(value);
            stringRedisTemplate.opsForValue().set(cacheKey, json, 10, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            throw new BusinessException("缓存写入失败");
        }
    }

    /**
     * 缓存空对象，防缓存穿透
     * @param cacheKey 缓存键
     */
    public void setEmptyCache(String cacheKey) {
        stringRedisTemplate.opsForValue().set(cacheKey, CacheConstants.CACHE_EMPTY_VALUE, 1, TimeUnit.MINUTES);
    }

    /**
     * 判断缓存是否为空
     * @param cachedJson 缓存值
     * @return 是否为空
     */
    public boolean isEmptyCache(String cachedJson) {
        return CacheConstants.CACHE_EMPTY_VALUE.equals(cachedJson);
    }
    
}
