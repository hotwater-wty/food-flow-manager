package com.foodflow.module.dish.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.foodflow.module.dish.dto.DishCreateDTO;
import com.foodflow.module.dish.dto.DishUpdateDTO;
import com.foodflow.module.dish.entity.Dish;
import com.foodflow.module.dish.vo.DishVO;

import jakarta.validation.Valid;

public interface DishService extends IService<Dish> {

    DishVO createDish(@Valid DishCreateDTO dishCreateDTO);

    void deleteById(Long dishId);

    DishVO getDishById(Long dishId);

    List<DishVO> getDishList();

    DishVO updateDish(Long dishId, @Valid DishUpdateDTO dishUpdateDTO);

    void updateDishStatus(Long dishId, Integer status);

    List<DishVO> getEnabledDishList(Long categoryId);

}
