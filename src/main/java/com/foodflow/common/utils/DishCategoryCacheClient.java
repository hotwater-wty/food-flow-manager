package com.foodflow.common.utils;

import java.util.Set;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.foodflow.common.constant.CacheConstants;
import com.foodflow.common.exception.BusinessException;
import com.foodflow.common.result.CacheResult;
import com.foodflow.module.dishcategory.entity.DishCategory;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DishCategoryCacheClient {
    private final CacheUtil cacheUtil;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 构建启用菜品缓存键
     * @param categoryId 分类ID(可选)
     * @return 缓存键
     */
    public String buildOnSaleDishCacheKey(Long categoryId) {
        if (categoryId == null) {
            return CacheConstants.DISH_ON_SALE_ALL_KEY;
        }
        return CacheConstants.DISH_ON_SALE_CATEGORY_PREFIX + categoryId;
    }

    /**
     * 设置菜品分类空缓存，防缓存穿透
     * @param categoryId 分类ID
     */
    public void setDishCategoryEmptyCache(Long categoryId) {
        cacheUtil.setEmptyCache(buildOnSaleDishCacheKey(categoryId));
    }

    /**
     * 设置启用分类缓存
     * @param categoryId 分类ID
     * @param category 分类
     */
    public void setDishCategoryCache(Long categoryId, DishCategory category) {
        cacheUtil.setCache(buildOnSaleDishCacheKey(categoryId), category);
    }

    /**
     * 从缓存中获取启用分类
     * @param categoryId 分类ID
     * @return 分类
     */
    public DishCategory getDishCategoryCache(Long categoryId) {
        CacheResult<DishCategory> cacheResult = cacheUtil.getCache(
            buildOnSaleDishCacheKey(categoryId), DishCategory.class);
        if (cacheResult.isEmpty()) {
            throw new BusinessException("分类不存在");
        }
        if (cacheResult.isMiss()) {
            return null;
        }
        return cacheResult.getData();
    }

    /**
     * 清空启用分类缓存
     */
    public void cleanCategoryCache() {
        stringRedisTemplate.delete(CacheConstants.CATEGORY_ENABLED_LIST_KEY);
        Set<String> categoryKeys = stringRedisTemplate.keys(CacheConstants.CATEGORY_ENABLED_PREFIX + "*");
        if (categoryKeys != null && !categoryKeys.isEmpty()) {
            stringRedisTemplate.delete(categoryKeys);
        }

        // 清空启售菜品缓存
        stringRedisTemplate.delete(CacheConstants.DISH_ON_SALE_ALL_KEY);
        Set<String> dishKeys = stringRedisTemplate.keys(CacheConstants.DISH_ON_SALE_CATEGORY_PREFIX + "*");
        if (dishKeys != null && !dishKeys.isEmpty()) {
            stringRedisTemplate.delete(dishKeys);
        }
    }
}
