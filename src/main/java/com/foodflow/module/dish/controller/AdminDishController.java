package com.foodflow.module.dish.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import com.foodflow.common.dto.PageQueryDTO;
import com.foodflow.common.result.PageResult;
import com.foodflow.common.result.Result;
import com.foodflow.module.dish.dto.DishCreateDTO;
import com.foodflow.module.dish.dto.DishUpdateDTO;
import com.foodflow.module.dish.service.DishService;
import com.foodflow.module.dish.vo.DishVO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/dishes")
@Tag(name = "管理端-菜品管理", description = "管理端菜品新增、查询、修改、删除与上下架接口")
public class AdminDishController {
    
    private final DishService dishService;

    /**
     * 创建菜品
     * 
     * @param dishCreateDTO 菜品创建DTO
     * @return 菜品VO
     */
    @PostMapping
    @Operation(summary = "新增菜品", description = "管理端创建菜品，默认可指定菜品所属分类、价格、图片、描述和状态")
    public Result<DishVO> createDish(@Valid @RequestBody DishCreateDTO dishCreateDTO) {
        log.info("创建菜品: {}", dishCreateDTO);
        DishVO dishVO = dishService.createDish(dishCreateDTO);
        return Result.success(dishVO);
    }

    /**
     * 删除菜品
     * 
     * @param dishId 菜品ID
     * @return 无
     */
    @DeleteMapping("/{dishId}")
    @Operation(summary = "删除菜品", description = "根据菜品ID删除菜品")
    public Result<Void> deleteDish(@Parameter(description = "菜品ID", example = "1") @PathVariable Long dishId) {
        log.info("删除菜品: {}", dishId);
        dishService.deleteById(dishId);
        return Result.success();
    }

    /**
     * 获取菜品详情
     * 
     * @param dishId 菜品ID
     * @return 菜品VO
    */
    @GetMapping("/{dishId}")
    @Operation(summary = "查询菜品详情", description = "根据菜品ID查询菜品详细信息")
    public Result<DishVO> getDish(@Parameter(description = "菜品ID", example = "1") @PathVariable Long dishId) {
        log.info("获取菜品详情: {}", dishId);
        DishVO dishVO = dishService.getDishById(dishId);
        return Result.success(dishVO);
    }

    /**
     * 获取菜品列表
     * 
     * @param pageQueryDTO 分页查询DTO
     * @return 菜品VO列表
     */
    @GetMapping
    @Operation(summary = "分页查询菜品列表", description = "管理端分页查询全部菜品列表")
    public Result<PageResult<DishVO>> getDishList(@ParameterObject @Validated PageQueryDTO pageQueryDTO) {
        log.info("获取菜品列表, pageQueryDTO: {}", pageQueryDTO);
        PageResult<DishVO> dishVOList = dishService.getDishList(pageQueryDTO);
        return Result.success(dishVOList);
    }

    /**
     * 更新菜品
     * @param dishUpdateDTO 菜品修改DTO
     * @return 菜品VO
     */
    @PutMapping("/{dishId}")
    @Operation(summary = "修改菜品", description = "根据菜品ID修改菜品基础信息")
    public Result<DishVO> updateDish(@Parameter(description = "菜品ID", example = "1") @PathVariable Long dishId,
            @Valid @RequestBody DishUpdateDTO dishUpdateDTO) {
        log.info("更新菜品: {}", dishId);
        DishVO dishVO = dishService.updateDish(dishId, dishUpdateDTO);
        return Result.success(dishVO);
    }

    /**
     * 修改菜品状态
     *
     * @param dishId 菜品ID
     * @param status 菜品状态
     * @return 无
     */
    @PostMapping("/{dishId}/status")
    @Operation(summary = "修改菜品状态", description = "管理端修改菜品售卖状态，0-停售，1-启售，2-售罄")
    public Result<Void> updateDishStatus(
            @Parameter(description = "菜品ID", example = "1") @PathVariable Long dishId,
            @Parameter(description = "菜品状态：0-停售，1-启售，2-售罄", example = "1") @RequestParam Integer status) {
        log.info("修改菜品状态: {}, {}", dishId, status);
        dishService.updateDishStatus(dishId, status);
        return Result.success();
    }
}
