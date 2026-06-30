package com.foodflow.module.dishcategory.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodflow.common.constant.CacheConstants;
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

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 创建分类
     * @param dishCategoryDTO 分类DTO
     * @return 创建后的分类VO
     */
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
        cleanCategoryCache();
        return toVO(category);
    }

    /**
     * 更新分类
     * @param categoryId 分类ID
     * @param dishCategoryDTO 分类DTO
     * @return 更新后的分类VO
     */
    @Override
    public DishCategoryVO updateCategory(Long categoryId, DishCategoryDTO dishCategoryDTO) {
        DishCategory category = getExistingCategory(categoryId);
        category.setName(dishCategoryDTO.getName());
        category.setSort(dishCategoryDTO.getSort());
        category.setUpdateTime(LocalDateTime.now());
        updateById(category);
        cleanCategoryCache();
        return toVO(category);
    }

    @Override
    public void deleteCategory(Long categoryId) {
        getExistingCategory(categoryId);
        removeById(categoryId);
        cleanCategoryCache();
    }

    @Override
    public DishCategoryVO getCategoryById(Long categoryId) {
        return toVO(getExistingCategory(categoryId));
    }

    /**
     * 管理员获取分类列表
     * @param pageQueryDTO 分页查询参数
     * @return 分页结果
     */
    @Override
    public PageResult<DishCategoryVO> getAdminCategoryList(PageQueryDTO pageQueryDTO) {
        Page<DishCategory> pageParam = new Page<>(pageQueryDTO.getPageNo(), pageQueryDTO.getPageSize());
        Page<DishCategory> categoryPage = page(pageParam, query()
                .orderByAsc("sort")
                .getWrapper());
        List<DishCategoryVO> records = categoryPage.getRecords().stream()
                .map(this::toVO)
                .toList();
        return new PageResult<>(
                categoryPage.getTotal(),
                pageQueryDTO.getPageNo(),
                pageQueryDTO.getPageSize(),
                records);
    }

    /**
     * 获取启用的分类列表
     * @return 启用分类列表
     */
    @Override
    public List<DishCategoryVO> getEnabledCategoryList() {
        String cachedKey = CacheConstants.CATEGORY_ENABLED_LIST_KEY;
        String cachedListJson = stringRedisTemplate.opsForValue().get(cachedKey);
        if (cachedListJson != null) {
            try {
                return objectMapper.readValue(
                    cachedListJson, 
                    new TypeReference<List<DishCategoryVO>>() {});
            } catch (JsonProcessingException e) {
                throw new BusinessException("解析启用分类列表失败");
            }
        }
        List<DishCategoryVO> records = lambdaQuery()
                .eq(DishCategory::getStatus, CategoryStatusEnum.ENABLED)
                .orderByAsc(DishCategory::getSort)
                .list().stream()
                .map(this::toVO)
                .toList();
        try{
            String listJson = objectMapper.writeValueAsString(records);
            stringRedisTemplate.opsForValue().set(cachedKey, listJson, 10, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            throw new BusinessException("序列化启用分类列表失败");
        }
        return records;
    }

    /**
     * 启用菜品分类
     * @param categoryId 分类id
     */
    @Override
    public void enableCategory(Long categoryId) {
        DishCategory category = getExistingCategory(categoryId);
        if (category.getStatus() == CategoryStatusEnum.ENABLED) {
            throw new BusinessException("分类已启用");
        }
        
        boolean enabled = lambdaUpdate()
                        .eq(DishCategory::getId, categoryId)
                        .ne(DishCategory::getStatus, CategoryStatusEnum.ENABLED)
                        .set(DishCategory::getStatus, CategoryStatusEnum.ENABLED)
                        .set(DishCategory::getUpdateTime, LocalDateTime.now())
                        .update();
        if (!enabled) {
            throw new BusinessException("启用分类失败");
        }
        
        cleanCategoryCache();
    }

    /**
     * 禁用菜品分类
     * @param categoryId 分类id
     */
    @Override
    public void disableCategory(Long categoryId) {
        DishCategory category = getExistingCategory(categoryId);
        if (category.getStatus() == CategoryStatusEnum.DISABLED) {
            throw new BusinessException("分类已禁用");
        }
        
        boolean disabled = lambdaUpdate()
                        .eq(DishCategory::getId, categoryId)
                        .ne(DishCategory::getStatus, CategoryStatusEnum.DISABLED)
                        .set(DishCategory::getStatus, CategoryStatusEnum.DISABLED)
                        .set(DishCategory::getUpdateTime, LocalDateTime.now())
                        .update();
        if (!disabled) {
            throw new BusinessException("禁用分类失败");
        }
        
        cleanCategoryCache();
    }

    /**
     * 从数据库获取存在的分类并校验
     * @param categoryId 分类ID
     * @return 存在的分类
     */
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

    /**
     * 清空启用分类缓存
     */
    private void cleanCategoryCache() {
        stringRedisTemplate.delete(CacheConstants.CATEGORY_ENABLED_LIST_KEY);
        Set<String> keys = stringRedisTemplate.keys(CacheConstants.CATEGORY_ENABLED_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
    }
}
