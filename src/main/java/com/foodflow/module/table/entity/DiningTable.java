package com.foodflow.module.table.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("dining_table")
public class DiningTable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String tableNo;

    private Integer capacity;

    private String locationDesc;

    private Integer status;

    private Long currentSessionId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
