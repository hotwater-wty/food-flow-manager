package com.foodflow.common.constant;

/**
 * redis缓存键前缀
 */
public class CacheConstants {

    //用户状态缓存键前缀
    public static final String USER_STATUS_CACHE_KEY = "foodflow:account:user:status:";
    //员工状态缓存键前缀
    public static final String EMPLOYEE_STATUS_CACHE_KEY = "foodflow:account:employee:status:";
    // 用户端查询所有上架菜品信息的缓存键
    public static final String DISH_ON_SALE_ALL_KEY = "foodflow:dish:on-sale:all";
    // 用户端按分类查询上架菜品信息的缓存键前缀
    public static final String DISH_ON_SALE_CATEGORY_PREFIX = "foodflow:dish:on-sale:category:";

    private CacheConstants() {
    }
}
