package com.foodflow.module.diningsession.dto;

import com.foodflow.common.enums.DiningSessionStatusEnum;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 用餐会话DTO，条件查询用参数
 */
@Data
@Schema(description = "堂食会话查询条件")
public class DiningSessionDTO {

    @Schema(description = "预约ID", example = "1")
    private Long reservationId;

    @Schema(description = "桌位ID", example = "1")
    private Long tableId;

    @Schema(description = "堂食会话ID", example = "1")
    private Long sessionId;

    @Schema(description = "堂食会话状态：0-等待中，1-用餐中，2-已完成，3-已取消", example = "1")
    private Integer status;

    @Schema(description = "页码", example = "1")
    @Min(value = 1, message = "页码必须大于等于1")
    private Integer pageNo = 1;

    @Schema(description = "每页数量", example = "10")
    @Min(value = 1, message = "每页数量必须大于等于1")
    @Max(value = 500, message = "每页数量不能超过500")
    private Integer pageSize = 10;

    public DiningSessionStatusEnum getStatusEnum() {
        return status == null ? null : DiningSessionStatusEnum.ofCode(status);
    }
}
