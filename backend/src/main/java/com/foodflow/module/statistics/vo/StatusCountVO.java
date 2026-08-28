package com.foodflow.module.statistics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "今日订单状态数量")
public class StatusCountVO {

    @Schema(description = "订单状态码：1-已下单，2-制作中，3-已上齐，4-已完成，5-已取消（不含取消）", example = "1")
    private Integer status;

    @Schema(description = "该状态的今日订单数量", example = "3")
    private Long count;
}
