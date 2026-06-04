package com.foodflow.module.dishcategory.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodflow.common.result.Result;
import com.foodflow.module.dishcategory.dto.DishCategoryDTO;
import com.foodflow.module.dishcategory.service.DishCategoryService;
import com.foodflow.module.dishcategory.vo.DishCategoryVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/dish-categories")
public class AdminDishCategoryController {

    private final DishCategoryService dishCategoryService;

    @PostMapping
    public Result<DishCategoryVO> createCategory(@Validated @RequestBody DishCategoryDTO dishCategoryDTO) {
        log.info("创建菜品分类: {}", dishCategoryDTO);
        return Result.success(dishCategoryService.createCategory(dishCategoryDTO));
    }

    @GetMapping
    public Result<List<DishCategoryVO>> getCategoryList() {
        log.info("获取菜品分类列表");
        return Result.success(dishCategoryService.getAdminCategoryList());
    }

    @GetMapping("/{categoryId}")
    public Result<DishCategoryVO> getCategoryById(@PathVariable Long categoryId) {
        log.info("获取菜品分类详情: {}", categoryId);
        return Result.success(dishCategoryService.getCategoryById(categoryId));
    }

    @PutMapping("/{categoryId}")
    public Result<DishCategoryVO> updateCategory(@PathVariable Long categoryId,
            @Validated @RequestBody DishCategoryDTO dishCategoryDTO) {
        log.info("更新菜品分类: {}", categoryId);
        return Result.success(dishCategoryService.updateCategory(categoryId, dishCategoryDTO));
    }

    @DeleteMapping("/{categoryId}")
    public Result<Void> deleteCategory(@PathVariable Long categoryId) {
        log.info("删除菜品分类: {}", categoryId);
        dishCategoryService.deleteCategory(categoryId);
        return Result.success();
    }

    @PostMapping("/{categoryId}/enable")
    public Result<Void> enableCategory(@PathVariable Long categoryId) {
        log.info("启用菜品分类: {}", categoryId);
        dishCategoryService.enableCategory(categoryId);
        return Result.success();
    }

    @PostMapping("/{categoryId}/disable")
    public Result<Void> disableCategory(@PathVariable Long categoryId) {
        log.info("禁用菜品分类: {}", categoryId);
        dishCategoryService.disableCategory(categoryId);
        return Result.success();
    }
}
