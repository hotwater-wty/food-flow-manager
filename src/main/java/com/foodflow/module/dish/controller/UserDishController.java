package com.foodflow.module.dish.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.foodflow.common.result.Result;
import com.foodflow.module.dish.service.DishService;
import com.foodflow.module.dish.vo.DishVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/dishes")
public class UserDishController {
    private final DishService dishService;

    /**
     * 用户获取启售菜品
     * 
     * @return 菜品VO
    */
    @GetMapping
    public Result<List<DishVO>> getDishList(@RequestParam(required = false) Long categoryId) {
        // TODO v2做分页查询
        log.info("获取用户启售中的菜品");
        List<DishVO> dishVOList = dishService.getEnabledDishList(categoryId);
        return Result.success(dishVOList);
    }
}
