package com.foodflow.module.dishcategory.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodflow.common.result.Result;
import com.foodflow.module.dishcategory.service.DishCategoryService;
import com.foodflow.module.dishcategory.vo.DishCategoryVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/dish-categories")
@Tag(name = "用户端-菜品分类查询", description = "用户端启用菜品分类查询接口")
public class UserDishCategoryController {

    private final DishCategoryService dishCategoryService;

    @GetMapping
    @Operation(summary = "查询启用菜品分类列表", description = "用户端查询当前可用的菜品分类")
    public Result<List<DishCategoryVO>> getEnabledCategoryList() {
        log.info("用户获取启用菜品分类列表");
        return Result.success(dishCategoryService.getEnabledCategoryList());
    }
}
