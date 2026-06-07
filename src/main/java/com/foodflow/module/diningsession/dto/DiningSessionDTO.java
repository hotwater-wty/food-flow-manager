package com.foodflow.module.diningsession.dto;

import com.foodflow.common.enums.DiningSessionStatusEnum;

import lombok.Data;

/**
 * 用餐会话DTO，条件查询用参数
 */
@Data
public class DiningSessionDTO {

    private Long reservationId;
    private Long tableId;
    private Long sessionId;
    private Integer status;

    public DiningSessionStatusEnum getStatusEnum() {
        return status == null ? null : DiningSessionStatusEnum.ofCode(status);
    }
}
