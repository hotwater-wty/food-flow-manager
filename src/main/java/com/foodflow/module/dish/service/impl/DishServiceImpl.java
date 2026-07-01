package com.foodflow.module.dish.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodflow.common.constant.CacheConstants;
import com.foodflow.common.dto.PageQueryDTO;
import com.foodflow.common.enums.CategoryStatusEnum;
import com.foodflow.common.enums.DishStatusEnum;
import com.foodflow.common.exception.BusinessException;
import com.foodflow.common.result.PageResult;
import com.foodflow.common.utils.DishCacheClient;
import com.foodflow.common.utils.DishCategoryCacheClient;
import com.foodflow.common.utils.CacheUtil;
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
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    private final DishCategoryService dishCategoryService;
    private final CacheUtil cacheUtil;
    private final DishCacheClient dishCacheClient;
    private final DishCategoryCacheClient dishCategoryCacheClient;

    /**
     * 创建菜品
     * @param dishCreateDTO 创建菜品请求参数
     * @return 创建的菜品VO
     */
    @Override
    public DishVO createDish(DishCreateDTO dishCreateDTO) {
        // 检查菜品分类是否可用
        checkEnabledCategory(dishCreateDTO.getCategoryId());
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
        dishCacheClient.cleanDishCache();
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
        dishCacheClient.cleanDishCache();
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
     * 分页查询菜品列表(管理员端)
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
        dishCacheClient.cleanDishCache();
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
        dishCacheClient.cleanDishCache();
    }

    /**
     * 启售菜品列表查询
     * @param categoryId 菜品分类ID(可选)
     * @return 菜品VO列表
     */
    @Override
    public List<DishVO> getEnabledDishList(Long categoryId) {
        // 从缓存中获取
        List<DishVO> cachedList = dishCacheClient.getOnSaleDishListCache(categoryId);
        if (cachedList != null) {
            return cachedList;
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
        dishCacheClient.setOnSaleDishListCache(categoryId, result);
        return result;
    }

    /**
     * 查询菜品详情
     * @param dishId 菜品ID
     * @return 菜品VO
     */
    @Override
    public DishVO getDishDetail(Long dishId) {
        if (dishId == null) {
            throw new BusinessException("菜品ID不能为空");
        }
        // 从缓存中获取
        DishVO cachedVO = dishCacheClient.getDishDetailCache(dishId);
        if (cachedVO != null) {
            return cachedVO;
        }
        
        // 从数据库查询
        Dish dish = getById(dishId);
        if (dish == null) {
            // 缓存空对象，防缓存穿透
            dishCacheClient.setDishDetailEmptyCache(dishId);
            throw new BusinessException("菜品不存在");
        }
        if (dish.getStatus() != DishStatusEnum.ON_SALE) {
            dishCacheClient.setDishDetailEmptyCache(dishId);
            throw new BusinessException("菜品已下架");
        }
        if (dish.getStatus() != DishStatusEnum.SOLD_OUT) {
            dishCacheClient.setDishDetailEmptyCache(dishId);
            throw new BusinessException("菜品已售罄");
        }

        // 转换为VO
        DishVO dishVO = toDishVO(dish);

        // 缓存结果
        dishCacheClient.setDishDetailCache(dishId, dishVO);
        
        return dishVO;
    }

    /**
     * 检查菜品分类是否存在且已启用
     * @param categoryId 菜品分类ID
     */
    private void checkEnabledCategory(Long categoryId) {
        // 从缓存中获取
        DishCategory cachedCategory = dishCategoryCacheClient.getDishCategoryCache(categoryId);
        if (cachedCategory != null) {
            return;
        }
        
        // 检查菜品分类是否存在且已启用
        DishCategory category = dishCategoryService.getById(categoryId);
        if (category == null) {
            // 缓存空对象，防缓存穿透
            dishCategoryCacheClient.setDishCategoryEmptyCache(categoryId);
            throw new BusinessException("菜品分类不存在");
        }
        if (category.getStatus() != CategoryStatusEnum.ENABLED) {
            // 缓存空对象，防缓存穿透
            dishCategoryCacheClient.setDishCategoryEmptyCache(categoryId);
            throw new BusinessException("菜品分类已禁用");
        }

        // 缓存启用分类
        dishCategoryCacheClient.setDishCategoryCache(categoryId, category);
    }
    
}
