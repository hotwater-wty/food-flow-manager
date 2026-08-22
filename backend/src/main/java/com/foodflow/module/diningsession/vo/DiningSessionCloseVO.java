package com.foodflow.module.diningsession.vo;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "关闭堂食会话响应数据")
public class DiningSessionCloseVO {
    @Schema(description = "堂食会话ID", example = "1")
    private Long sessionId;

    @Schema(description = "堂食会话编号", example = "S202606080001")
    private String sessionNo;

    @Schema(description = "桌位ID", example = "1")
    private Long tableId;

    @Schema(description = "桌号", example = "A01")
    private String tableNo;

    @Schema(description = "堂食会话状态：0-等待中，1-用餐中，2-已完成，3-已取消", example = "2")
    private Integer sessionStatus;

    @Schema(description = "桌位状态：0-空闲，1-已预约，2-等待中，3-用餐中，4-禁用", example = "0")
    private Integer tableStatus;

    @Schema(description = "关闭时间", example = "2026-06-08 20:30:00")
    private LocalDateTime closeTime;

    @Schema(description = "执行清台的员工ID", example = "1")
    private Long closeEmployeeId;
}
