package com.foodflow.module.dish.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodflow.common.enums.DishStatusEnum;
import com.foodflow.common.exception.BusinessException;
import com.foodflow.module.dish.dto.DishCreateDTO;
import com.foodflow.module.dish.dto.DishUpdateDTO;
import com.foodflow.module.dish.entity.Dish;
import com.foodflow.module.dish.mapper.DishMapper;
import com.foodflow.module.dish.service.DishService;
import com.foodflow.module.dish.vo.DishVO;

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

    @Override
    public DishVO createDish(DishCreateDTO dishCreateDTO) {
        Dish dish = Dish.builder()
                .categoryId(dishCreateDTO.getCategoryId())
                .name(dishCreateDTO.getName())
                .price(dishCreateDTO.getPrice())
                .image(dishCreateDTO.getImage())
                .description(dishCreateDTO.getDescription())
                .status(dishCreateDTO.getStatus())
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
                .status(dish.getStatus())
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
    public List<DishVO> getDishList() {
        return list().stream()
                .map(this::toDishVO)
                .collect(Collectors.toList());
    }

    @Override
    public DishVO updateDish(DishUpdateDTO dishUpdateDTO) {
        Dish dish = getById(dishUpdateDTO.getDishId());
        if (dish == null) {
            throw new BusinessException("菜品不存在");
        }
        dish.setCategoryId(dishUpdateDTO.getCategoryId());
        dish.setName(dishUpdateDTO.getName());
        dish.setPrice(dishUpdateDTO.getPrice());
        dish.setImage(dishUpdateDTO.getImage());
        dish.setDescription(dishUpdateDTO.getDescription());
        dish.setUpdateTime(LocalDateTime.now());
        saveOrUpdate(dish);
        return toDishVO(dish);
    }
    
}
