package com.foodflow.common.utils;

import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.foodflow.common.constant.CacheConstants;
import com.foodflow.common.exception.BusinessException;
import com.foodflow.common.result.CacheResult;
import com.foodflow.module.dish.vo.DishVO;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DishCacheClient {
    private final CacheUtil cacheUtil;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 构建菜品详情缓存键
     * @param dishId 菜品ID
     * @return 缓存键
     */
    public String buildDishDetailCacheKey(Long dishId) {
        return CacheConstants.DISH_DETAIL_PREFIX + dishId;
    }

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
    
    /*
     * 菜品详情缓存
     */ 

    /**
     * 设置菜品详情空缓存，防缓存穿透
     * @param dishId 菜品ID
     */
    public void setDishDetailEmptyCache(Long dishId) {
        cacheUtil.setEmptyCache(buildDishDetailCacheKey(dishId));
    }

    /**
     * 从缓存中获取菜品详情
     * @param dishId 菜品ID
     * @return 菜品VO
     */
    public DishVO getDishDetailCache(Long dishId) {
        CacheResult<DishVO> cacheResult = cacheUtil.getCache(
            buildDishDetailCacheKey(dishId), DishVO.class);
        if (cacheResult.isEmpty()) {
            throw new BusinessException("菜品不存在");
        }
        if (cacheResult.isMiss()) {
            return null;
        }
        return cacheResult.getData();
    }

    /**
     * 设置菜品详情缓存
     * @param dishId 菜品ID
     * @param dishVO 菜品VO
     */
    public void setDishDetailCache(Long dishId, DishVO dishVO) {
        cacheUtil.setCache(buildDishDetailCacheKey(dishId), dishVO);
    }

    /*
     * 启用菜品缓存
     */ 

    /**
     * 从缓存中获取启用菜品列表
     * @param categoryId 分类ID(可选)
     * @return 菜品VO列表
     */
    public List<DishVO> getOnSaleDishListCache(Long categoryId) {
        CacheResult<List<DishVO>> cacheResult = cacheUtil.getCache(
            buildOnSaleDishCacheKey(categoryId), new TypeReference<List<DishVO>>() {});
        if (cacheResult.isEmpty()) {
            throw new BusinessException("启用菜品列表不存在");
        }
        if (cacheResult.isMiss()) {
            return null;
        }
        return cacheResult.getData();
    }
    
    /**
     * 设置启用菜品缓存
     * @param categoryId 分类ID(可选)
     * @param dishVOList 菜品VO列表
     */
    public void setOnSaleDishListCache(Long categoryId, List<DishVO> dishVOList) {
        cacheUtil.setCache(buildOnSaleDishCacheKey(categoryId), dishVOList);
    }

    /**
     * 清理菜品缓存
     */
    public void cleanDishCache() {
        stringRedisTemplate.delete(CacheConstants.DISH_ON_SALE_ALL_KEY);
        Set<String> keys = stringRedisTemplate.keys(CacheConstants.DISH_ON_SALE_CATEGORY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
    }

}