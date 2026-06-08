package com.foodflow.module.table.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "桌位响应数据")
public class TableVO {
    @Schema(description = "桌位ID", example = "1")
    private Long tableId;

    @Schema(description = "桌号", example = "A01")
    private String tableNo;

    @Schema(description = "可容纳人数", example = "4")
    private Integer capacity;

    @Schema(description = "桌位位置描述", example = "大厅靠窗")
    private String locationDesc;

    @Schema(description = "桌位状态：0-空闲，1-已预约，2-等待中，3-用餐中，4-禁用", example = "0")
    private Integer status;

    @Schema(description = "当前堂食会话ID，空闲时为空", example = "1")
    private Long currentSessionId;
}
