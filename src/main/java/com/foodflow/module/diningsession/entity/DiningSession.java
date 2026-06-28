package com.foodflow.module.diningsession.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.foodflow.common.enums.ActiveFlagEnum;
import com.foodflow.common.enums.DiningSessionStatusEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiningSession {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sessionNo;

    private Long userId;

    private Long tableId;

    private Long reservationId;

    private DiningSessionStatusEnum status;

    // 数据库中活跃会话唯一表示，null: 未激活, ACTIVE: 已激活
    private ActiveFlagEnum activeFlag;     

    private LocalDateTime openTime;

    private LocalDateTime firstOrderTime;

    private LocalDateTime closeTime;

    private Long closeEmployeeId;

    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
