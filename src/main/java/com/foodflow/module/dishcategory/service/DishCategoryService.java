package com.foodflow.module.dishcategory.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.foodflow.module.dishcategory.dto.DishCategoryDTO;
import com.foodflow.module.dishcategory.entity.DishCategory;
import com.foodflow.module.dishcategory.vo.DishCategoryVO;

public interface DishCategoryService extends IService<DishCategory> {

    DishCategoryVO createCategory(DishCategoryDTO dishCategoryDTO);

    DishCategoryVO updateCategory(Long categoryId, DishCategoryDTO dishCategoryDTO);

    void deleteCategory(Long categoryId);

    DishCategoryVO getCategoryById(Long categoryId);

    List<DishCategoryVO> getAdminCategoryList();

    List<DishCategoryVO> getEnabledCategoryList();

    void enableCategory(Long categoryId);

    void disableCategory(Long categoryId);
}
