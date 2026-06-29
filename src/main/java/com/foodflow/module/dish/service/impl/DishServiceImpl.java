package com.foodflow.module.dish.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodflow.common.dto.PageQueryDTO;
import com.foodflow.common.enums.CategoryStatusEnum;
import com.foodflow.common.enums.DishStatusEnum;
import com.foodflow.common.exception.BusinessException;
import com.foodflow.common.result.PageResult;
import com.foodflow.module.dish.dto.DishCreateDTO;
import com.foodflow.module.dish.dto.DishUpdateDTO;
import com.foodflow.module.dish.entity.Dish;
import com.foodflow.module.dish.mapper.DishMapper;
import com.foodflow.module.dish.service.DishService;
import com.foodflow.module.dish.vo.DishVO;
import com.foodflow.module.dishcategory.entity.DishCategory;
import com.foodflow.module.dishcategory.service.DishCategoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    private final DishCategoryService dishCategoryService;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    // private static final String DISH_PAGE_CACHE_PREFIX = "foodflow:dish:page:";

    // private String buildDishPageCacheKey(PageQueryDTO pageQueryDTO) {
    // return DISH_PAGE_CACHE_PREFIX
    //         + pageQueryDTO.getPageNo()
    //         + ":"
    //         + pageQueryDTO.getPageSize();
    // }

    // 用户端查询菜品信息的缓存键
    private static final String DISH_ON_SALE_ALL_KEY = "foodflow:dish:on-sale:all";
    private static final String DISH_ON_SALE_CATEGORY_PREFIX = "foodflow:dish:on-sale:category:";

    private String buildOnSaleDishCacheKey(Long categoryId) {
        if (categoryId == null) {
            return DISH_ON_SALE_ALL_KEY;
        }
        return DISH_ON_SALE_CATEGORY_PREFIX + categoryId;
    }

    /**
     * 创建菜品
     * @param dishCreateDTO 创建菜品请求参数
     * @return 创建的菜品VO
     */
    @Override
    public DishVO createDish(DishCreateDTO dishCreateDTO) {
        if (dishCreateDTO.getCategoryId() != null) {
            checkEnabledCategory(dishCreateDTO.getCategoryId());
        }
        Dish dish = Dish.builder()
                .categoryId(dishCreateDTO.getCategoryId())
                .name(dishCreateDTO.getName())
                .price(dishCreateDTO.getPrice())
                .image(dishCreateDTO.getImage())
                .description(dishCreateDTO.getDescription())
                .status(DishStatusEnum.ofCode(dishCreateDTO.getStatus()))
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        save(dish);
        // 清除缓存
        cleanDishCache();
        return toDishVO(dish);
    }

    private DishVO toDishVO(Dish dish) {
        return DishVO.builder()
                .id(dish.getId())
                .categoryId(dish.getCategoryId())
                .name(dish.getName())
                .price(dish.getPrice())
                .image(dish.getImage())
                .description(dish.getDescription())
                .status(dish.getStatus().getCode())
                .build();
    }

    /**
     * 删除菜品
     * @param dishId 菜品ID
     */
    @Override
    public void deleteById(Long dishId) {
        Dish dish = getById(dishId);
        if (dish == null) {
            throw new BusinessException("菜品不存在");
        }
        removeById(dishId);
        // 清除缓存
        cleanDishCache();
    }

    @Override
    public DishVO getDishById(Long dishId) {
        Dish dish = getById(dishId);
        if (dish == null) {
            // TODO v2做缓存
            throw new BusinessException("菜品不存在");
        }
        return toDishVO(dish);
    }

    /**
     * 分页查询菜品列表
     * @param pageQueryDTO 分页查询参数
     * @return 分页结果集
     */
    @Override
    public PageResult<DishVO> getDishList(PageQueryDTO pageQueryDTO) {
        // 从数据库查询
        Page<Dish> pageParam = new Page<>(pageQueryDTO.getPageNo(), pageQueryDTO.getPageSize());
        Page<Dish> dishPage = page(pageParam, lambdaQuery()
                .orderByDesc(Dish::getCreateTime)
                .getWrapper());
        List<DishVO> records = dishPage.getRecords().stream()
                .map(this::toDishVO)
                .collect(Collectors.toList());
        
        // 构建分页结果集
        PageResult<DishVO> pageResult = new PageResult<>(
                dishPage.getTotal(),
                pageQueryDTO.getPageNo(),
                pageQueryDTO.getPageSize(),
                records);
        
        return pageResult;
    }

    /**
     * 更新菜品信息
     * @param dishId 菜品ID
     * @param dishUpdateDTO 菜品更新参数
     * @return 更新后的菜品VO
     */
    @Override
    public DishVO updateDish(Long dishId, DishUpdateDTO dishUpdateDTO) {
        Dish dish = getById(dishId);
        if (dish == null) {
            throw new BusinessException("菜品不存在");
        }

        // 检查菜品分类是否存在且已启用
        if (dishUpdateDTO.getCategoryId() != null) {
            checkEnabledCategory(dishUpdateDTO.getCategoryId());
        }

        // 更新菜品信息
        dish.setCategoryId(dishUpdateDTO.getCategoryId());
        dish.setName(dishUpdateDTO.getName());
        dish.setPrice(dishUpdateDTO.getPrice());
        dish.setImage(dishUpdateDTO.getImage());
        dish.setDescription(dishUpdateDTO.getDescription());
        dish.setUpdateTime(LocalDateTime.now());
        saveOrUpdate(dish);

        // 清除缓存
        cleanDishCache();
        return toDishVO(dish);
    }

    /**
     * 更新菜品状态
     * @param dishId 菜品ID
     * @param status 菜品状态
     */
    @Override
    public void updateDishStatus(Long dishId, Integer status) {
        Dish dish = getById(dishId);
        if (dish == null) {
            throw new BusinessException("菜品不存在");
        }
        dish.setStatus(DishStatusEnum.ofCode(status));
        dish.setUpdateTime(LocalDateTime.now());
        updateById(dish);
        cleanDishCache();
    }

    /**
     * 启售菜品查询
     * @param categoryId 菜品分类ID(可选)
     * @return 菜品VO列表
     */
    @Override
    public List<DishVO> getEnabledDishList(Long categoryId) {
        // 从缓存中获取
        String cacheKey = buildOnSaleDishCacheKey(categoryId);
        String cachedJson = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cachedJson != null) {
            try {
                return objectMapper.readValue(
                    cachedJson, 
                    new TypeReference<List<DishVO>>() {});
            } catch (JsonProcessingException e) {
                throw new BusinessException("菜品缓存解析失败");
            }
        }
        // 从数据库查询
        if (categoryId != null) {
            checkEnabledCategory(categoryId);
        }
        List<DishVO> result = lambdaQuery()
                .eq(categoryId != null, Dish::getCategoryId, categoryId)
                .eq(Dish::getStatus, DishStatusEnum.ON_SALE)
                .list().stream()
                .map(this::toDishVO)
                .toList();
        
        // 缓存结果
        try{
            String json = objectMapper.writeValueAsString(result);
            stringRedisTemplate.opsForValue().set(cacheKey, json, 10, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            throw new BusinessException("菜品缓存写入失败");
        }
        return result;
    }

    /**
     * 检查菜品分类是否存在且已启用
     * @param categoryId 菜品分类ID
     */
    private void checkEnabledCategory(Long categoryId) {
        // // 从缓存中获取
        // String cacheKey = DISH_ON_SALE_CATEGORY_PREFIX + categoryId;
        // Object cachedResult = redisTemplate.opsForValue().get(cacheKey);
        // if (cachedResult != null) {
        //     return;
        // }

        // 检查菜品分类是否存在且已启用
        DishCategory category = dishCategoryService.getById(categoryId);
        if (category == null) {
            throw new BusinessException("菜品分类不存在");
        }
        if (category.getStatus() != CategoryStatusEnum.ENABLED) {
            throw new BusinessException("菜品分类已禁用");
        }
    }

    /**
     * 清理菜品缓存
     */
    private void cleanDishCache() {
        stringRedisTemplate.delete(DISH_ON_SALE_ALL_KEY);
        Set<String> keys = stringRedisTemplate.keys(DISH_ON_SALE_CATEGORY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
    }
    
}
