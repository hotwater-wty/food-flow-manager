package com.foodflow.module.dish.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "用户端-菜品查询", description = "用户端可售菜品查询接口")
public class UserDishController {
    private final DishService dishService;

    /**
     * 用户获取启售菜品
     * 
     * @return 菜品VO
    */
    @GetMapping
    @Operation(summary = "查询可售菜品列表", description = "用户端查询启售中的菜品，可按菜品分类筛选")
    public Result<List<DishVO>> getDishList(
            @Parameter(description = "菜品分类ID，不传则查询全部可售菜品", example = "1")
            @RequestParam(required = false) Long categoryId) {
        // TODO v2做分页查询
        log.info("获取用户启售中的菜品");
        List<DishVO> dishVOList = dishService.getEnabledDishList(categoryId);
        return Result.success(dishVOList);
    }
}
