package com.foodflow.module.dishcategory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodflow.common.dto.PageQueryDTO;
import com.foodflow.common.result.PageResult;
import com.foodflow.common.result.Result;
import com.foodflow.module.dishcategory.dto.DishCategoryDTO;
import com.foodflow.module.dishcategory.service.DishCategoryService;
import com.foodflow.module.dishcategory.vo.DishCategoryVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springdoc.core.annotations.ParameterObject;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/dish-categories")
@Tag(name = "管理端-菜品分类管理", description = "管理端菜品分类新增、查询、修改、删除与启禁用接口")
public class AdminDishCategoryController {

    private final DishCategoryService dishCategoryService;

    @PostMapping
    @Operation(summary = "新增菜品分类", description = "管理端新增菜品分类")
    public Result<DishCategoryVO> createCategory(@Validated @RequestBody DishCategoryDTO dishCategoryDTO) {
        log.info("创建菜品分类: {}", dishCategoryDTO);
        return Result.success(dishCategoryService.createCategory(dishCategoryDTO));
    }

    @GetMapping
    @Operation(summary = "查询菜品分类列表", description = "管理端查询全部菜品分类")
    public Result<PageResult<DishCategoryVO>> getCategoryList(
            @ParameterObject @Validated PageQueryDTO pageQueryDTO) {
        log.info("获取菜品分类列表, pageQueryDTO: {}", pageQueryDTO);
        return Result.success(dishCategoryService.getAdminCategoryList(pageQueryDTO));
    }

    @GetMapping("/{categoryId}")
    @Operation(summary = "查询菜品分类详情", description = "根据分类ID查询菜品分类详情")
    public Result<DishCategoryVO> getCategoryById(
            @Parameter(description = "菜品分类ID", example = "1") @PathVariable Long categoryId) {
        log.info("获取菜品分类详情: {}", categoryId);
        return Result.success(dishCategoryService.getCategoryById(categoryId));
    }

    @PutMapping("/{categoryId}")
    @Operation(summary = "修改菜品分类", description = "根据分类ID修改菜品分类名称和排序")
    public Result<DishCategoryVO> updateCategory(
            @Parameter(description = "菜品分类ID", example = "1") @PathVariable Long categoryId,
            @Validated @RequestBody DishCategoryDTO dishCategoryDTO) {
        log.info("更新菜品分类: {}", categoryId);
        return Result.success(dishCategoryService.updateCategory(categoryId, dishCategoryDTO));
    }

    @DeleteMapping("/{categoryId}")
    @Operation(summary = "删除菜品分类", description = "根据分类ID删除菜品分类")
    public Result<Void> deleteCategory(
            @Parameter(description = "菜品分类ID", example = "1") @PathVariable Long categoryId) {
        log.info("删除菜品分类: {}", categoryId);
        dishCategoryService.deleteCategory(categoryId);
        return Result.success();
    }

    @PostMapping("/{categoryId}/enable")
    @Operation(summary = "启用菜品分类", description = "启用指定菜品分类")
    public Result<Void> enableCategory(
            @Parameter(description = "菜品分类ID", example = "1") @PathVariable Long categoryId) {
        log.info("启用菜品分类: {}", categoryId);
        dishCategoryService.enableCategory(categoryId);
        return Result.success();
    }

    @PostMapping("/{categoryId}/disable")
    @Operation(summary = "禁用菜品分类", description = "禁用指定菜品分类")
    public Result<Void> disableCategory(
            @Parameter(description = "菜品分类ID", example = "1") @PathVariable Long categoryId) {
        log.info("禁用菜品分类: {}", categoryId);
        dishCategoryService.disableCategory(categoryId);
        return Result.success();
    }
}
