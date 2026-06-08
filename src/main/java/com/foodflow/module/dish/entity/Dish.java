package com.foodflow.module.dish.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.foodflow.common.enums.DishStatusEnum;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Dish {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long categoryId;
    private String name;
    private Integer price;
    private String image;
    private String description;
    private DishStatusEnum status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
