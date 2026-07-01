package com.foodflow.common.constant;

/**
 * redis缓存键前缀
 */
public class CacheConstants {

    // 缓存空值
    public static final String CACHE_EMPTY_VALUE = "";

    //用户状态缓存键前缀
    public static final String USER_STATUS_CACHE_KEY = "foodflow:account:user:status:";
    //员工状态缓存键前缀
    public static final String EMPLOYEE_STATUS_CACHE_KEY = "foodflow:account:employee:status:";
    // 用户端查询所有上架菜品信息的缓存键
    public static final String DISH_ON_SALE_ALL_KEY = "foodflow:dish:on-sale:all";
    // 菜品详情缓存键前缀
    public static final String DISH_DETAIL_PREFIX = "foodflow:dish:detail:";
    // 用户端按分类查询上架菜品信息的缓存键前缀
    public static final String DISH_ON_SALE_CATEGORY_PREFIX = "foodflow:dish:on-sale:category:";
    // 启用分类列表 缓存键
    public static final String CATEGORY_ENABLED_LIST_KEY = "foodflow:dish-category:enabled:list";
    // 启用分类 缓存键前缀
    public static final String CATEGORY_ENABLED_PREFIX = "foodflow:dish-category:enabled:";
    
    private CacheConstants() {
    }
}
