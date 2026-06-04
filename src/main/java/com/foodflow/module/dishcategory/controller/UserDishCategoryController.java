package com.foodflow.module.dishcategory.controller;

import java.util.List;

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
public class UserDishCategoryController {

    private final DishCategoryService dishCategoryService;

    @GetMapping
    public Result<List<DishCategoryVO>> getEnabledCategoryList() {
        log.info("用户获取启用菜品分类列表");
        return Result.success(dishCategoryService.getEnabledCategoryList());
    }
}
