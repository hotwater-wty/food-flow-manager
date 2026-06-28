package com.foodflow.module.dishcategory.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodflow.common.dto.PageQueryDTO;
import com.foodflow.common.enums.CategoryStatusEnum;
import com.foodflow.common.exception.BusinessException;
import com.foodflow.common.result.PageResult;
import com.foodflow.module.dishcategory.dto.DishCategoryDTO;
import com.foodflow.module.dishcategory.entity.DishCategory;
import com.foodflow.module.dishcategory.mapper.DishCategoryMapper;
import com.foodflow.module.dishcategory.service.DishCategoryService;
import com.foodflow.module.dishcategory.vo.DishCategoryVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DishCategoryServiceImpl extends ServiceImpl<DishCategoryMapper, DishCategory>
        implements DishCategoryService {

    @Override
    public DishCategoryVO createCategory(DishCategoryDTO dishCategoryDTO) {
        DishCategory category = DishCategory.builder()
                .name(dishCategoryDTO.getName())
                .sort(dishCategoryDTO.getSort())
                .status(CategoryStatusEnum.ENABLED)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        save(category);
        return toVO(category);
    }

    @Override
    public DishCategoryVO updateCategory(Long categoryId, DishCategoryDTO dishCategoryDTO) {
        DishCategory category = getExistingCategory(categoryId);
        category.setName(dishCategoryDTO.getName());
        category.setSort(dishCategoryDTO.getSort());
        category.setUpdateTime(LocalDateTime.now());
        updateById(category);
        return toVO(category);
    }

    @Override
    public void deleteCategory(Long categoryId) {
        getExistingCategory(categoryId);
        removeById(categoryId);
    }

    @Override
    public DishCategoryVO getCategoryById(Long categoryId) {
        return toVO(getExistingCategory(categoryId));
    }

    @Override
    public PageResult<DishCategoryVO> getAdminCategoryList(PageQueryDTO pageQueryDTO) {
        Page<DishCategory> pageParam = new Page<>(pageQueryDTO.getPageNo(), pageQueryDTO.getPageSize());
        Page<DishCategory> categoryPage = page(pageParam, query()
                .orderByAsc("sort")
                .getWrapper());
        List<DishCategoryVO> records = categoryPage.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return new PageResult<>(
                categoryPage.getTotal(),
                pageQueryDTO.getPageNo(),
                pageQueryDTO.getPageSize(),
                records);
    }

    @Override
    public List<DishCategoryVO> getEnabledCategoryList() {
        return query()
                .eq("status", CategoryStatusEnum.ENABLED)
                .orderByAsc("sort")
                .list().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public void enableCategory(Long categoryId) {
        DishCategory category = getExistingCategory(categoryId);
        if (category.getStatus() == CategoryStatusEnum.ENABLED) {
            throw new BusinessException("分类已启用");
        }
        category.setStatus(CategoryStatusEnum.ENABLED);
        category.setUpdateTime(LocalDateTime.now());
        updateById(category);
    }

    @Override
    public void disableCategory(Long categoryId) {
        DishCategory category = getExistingCategory(categoryId);
        if (category.getStatus() == CategoryStatusEnum.DISABLED) {
            throw new BusinessException("分类已禁用");
        }
        category.setStatus(CategoryStatusEnum.DISABLED);
        category.setUpdateTime(LocalDateTime.now());
        updateById(category);
    }

    private DishCategory getExistingCategory(Long categoryId) {
        DishCategory category = getById(categoryId);
        if (category == null) {
            throw new BusinessException("菜品分类不存在");
        }
        return category;
    }

    private DishCategoryVO toVO(DishCategory category) {
        return DishCategoryVO.builder()
                .id(category.getId())
                .name(category.getName())
                .sort(category.getSort())
                .status(category.getStatus().getCode())
                .build();
    }
}
