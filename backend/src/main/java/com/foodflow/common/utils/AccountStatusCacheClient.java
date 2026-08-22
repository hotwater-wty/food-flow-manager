package com.foodflow.common.utils;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.foodflow.common.constant.CacheConstants;
import com.foodflow.common.result.CacheResult;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AccountStatusCacheClient {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 获取用户状态缓存
     * @param userId 用户ID
     * @return 用户状态缓存
     */
    public CacheResult<String> getUserStatusCache(Long userId) {
        return getStatusCache(buildUserStatusCacheKey(userId));
    }

    /**
     * 设置用户状态缓存
     * @param userId 用户ID
     * @param statusCode 状态码
     */
    public void setUserStatusCache(Long userId, Integer statusCode) {
        setStatusCache(buildUserStatusCacheKey(userId), statusCode);
    }

    /**
     * 设置用户状态缓存为空
     * @param userId 用户ID
     */
    public void setUserStatusEmptyCache(Long userId) {
        setEmptyStatusCache(buildUserStatusCacheKey(userId));
    }

    /**
     * 清除用户状态缓存
     * @param userId 用户ID
     */
    public void cleanUserStatusCache(Long userId) {
        stringRedisTemplate.delete(buildUserStatusCacheKey(userId));
    }

    /**
     * 获取员工状态缓存
     * @param employeeId 员工ID
     * @return 员工状态缓存
     */
    public CacheResult<String> getEmployeeStatusCache(Long employeeId) {
        return getStatusCache(buildEmployeeStatusCacheKey(employeeId));
    }

    /**
     * 设置员工状态缓存
     * @param employeeId 员工ID
     * @param statusCode 状态码
     */
    public void setEmployeeStatusCache(Long employeeId, Integer statusCode) {
        setStatusCache(buildEmployeeStatusCacheKey(employeeId), statusCode);
    }

    /**
     * 设置员工状态缓存为空
     * @param employeeId 员工ID
     */
    public void setEmployeeStatusEmptyCache(Long employeeId) {
        setEmptyStatusCache(buildEmployeeStatusCacheKey(employeeId));
    }

    /**
     * 清除员工状态缓存
     * @param employeeId 员工ID
     */
    public void cleanEmployeeStatusCache(Long employeeId) {
        stringRedisTemplate.delete(buildEmployeeStatusCacheKey(employeeId));
    }

    private String buildUserStatusCacheKey(Long userId) {
        return CacheConstants.USER_STATUS_CACHE_KEY + userId;
    }

    private String buildEmployeeStatusCacheKey(Long employeeId) {
        return CacheConstants.EMPLOYEE_STATUS_CACHE_KEY + employeeId;
    }

    private CacheResult<String> getStatusCache(String cacheKey) {
        String cachedStatus = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cachedStatus == null) {
            return CacheResult.miss();
        }
        if (CacheConstants.CACHE_EMPTY_VALUE.equals(cachedStatus)) {
            return CacheResult.empty();
        }
        return CacheResult.hit(cachedStatus);
    }

    private void setStatusCache(String cacheKey, Integer statusCode) {
        stringRedisTemplate.opsForValue().set(cacheKey, String.valueOf(statusCode), 10, TimeUnit.MINUTES);
    }

    private void setEmptyStatusCache(String cacheKey) {
        stringRedisTemplate.opsForValue().set(cacheKey, CacheConstants.CACHE_EMPTY_VALUE, 1, TimeUnit.MINUTES);
    }
}
