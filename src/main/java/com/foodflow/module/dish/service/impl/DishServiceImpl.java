package com.foodflow.module.dish.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    private final DishCategoryService dishCategoryService;

    @Override
    public DishVO createDish(DishCreateDTO dishCreateDTO) {
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

        saveOrUpdate(dish);
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

    @Override
    public void deleteById(Long dishId) {
        Dish dish = getById(dishId);
        if (dish == null) {
            throw new BusinessException("菜品不存在");
        }
        removeById(dishId);
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

    @Override
    public PageResult<DishVO> getDishList(PageQueryDTO pageQueryDTO) {
        Page<Dish> pageParam = new Page<>(pageQueryDTO.getPageNo(), pageQueryDTO.getPageSize());
        Page<Dish> dishPage = page(pageParam, query()
                .orderByDesc("create_time")
                .getWrapper());
        List<DishVO> records = dishPage.getRecords().stream()
                .map(this::toDishVO)
                .collect(Collectors.toList());
        return new PageResult<>(
                dishPage.getTotal(),
                pageQueryDTO.getPageNo(),
                pageQueryDTO.getPageSize(),
                records);
    }

    @Override
    public DishVO updateDish(Long dishId, DishUpdateDTO dishUpdateDTO) {
        Dish dish = getById(dishId);
        if (dish == null) {
            throw new BusinessException("菜品不存在");
        }
        checkEnabledCategory(dishUpdateDTO.getCategoryId());
        dish.setCategoryId(dishUpdateDTO.getCategoryId());
        dish.setName(dishUpdateDTO.getName());
        dish.setPrice(dishUpdateDTO.getPrice());
        dish.setImage(dishUpdateDTO.getImage());
        dish.setDescription(dishUpdateDTO.getDescription());
        dish.setUpdateTime(LocalDateTime.now());
        saveOrUpdate(dish);
        return toDishVO(dish);
    }

    @Override
    public void updateDishStatus(Long dishId, Integer status) {
        Dish dish = getById(dishId);
        if (dish == null) {
            throw new BusinessException("菜品不存在");
        }
        dish.setStatus(DishStatusEnum.ofCode(status));
        dish.setUpdateTime(LocalDateTime.now());
        updateById(dish);
    }

    @Override
    public List<DishVO> getEnabledDishList(Long categoryId) {
        return query()
                .eq(categoryId != null, "category_id", categoryId)
                .eq("status", DishStatusEnum.ON_SALE)
                .list().stream()
                .map(this::toDishVO)
                .collect(Collectors.toList());
    }

    private void checkEnabledCategory(Long categoryId) {
        DishCategory category = dishCategoryService.getById(categoryId);
        if (category == null) {
            throw new BusinessException("菜品分类不存在");
        }
        if (category.getStatus() != CategoryStatusEnum.ENABLED) {
            throw new BusinessException("菜品分类已禁用");
        }
    }
    
}
