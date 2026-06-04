package com.foodflow.module.dish.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import com.foodflow.common.result.Result;
import com.foodflow.module.dish.dto.DishCreateDTO;
import com.foodflow.module.dish.dto.DishUpdateDTO;
import com.foodflow.module.dish.service.DishService;
import com.foodflow.module.dish.vo.DishVO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/dishes")
public class AdminDishController {
    
    private final DishService dishService;

    /**
     * 创建菜品
     * 
     * @param dishCreateDTO 菜品创建DTO
     * @return 菜品VO
     */
    @PostMapping
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
    public Result<Void> deleteDish(@PathVariable Long dishId) {
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
    public Result<DishVO> getDish(@PathVariable Long dishId) {
        log.info("获取菜品详情: {}", dishId);
        DishVO dishVO = dishService.getDishById(dishId);
        return Result.success(dishVO);
    }

    @GetMapping
    public Result<List<DishVO>> getDishList() {
        log.info("获取菜品列表");
        List<DishVO> dishVOList = dishService.getDishList();
        return Result.success(dishVOList);
    }

    /**
     * 更新菜品
     * @param dishDTO 菜品创建DTO
     * @return 菜品VO
     */
    @PutMapping("/{dishId}")
    public Result<DishVO> updateDish(@PathVariable Long dishId,
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
    public Result<Void> updateDishStatus(@PathVariable Long dishId, @RequestParam Integer status) {
        log.info("修改菜品状态: {}, {}", dishId, status);
        dishService.updateDishStatus(dishId, status);
        return Result.success();
    }
}
