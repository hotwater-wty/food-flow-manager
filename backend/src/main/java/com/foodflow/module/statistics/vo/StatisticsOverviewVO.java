package com.foodflow.module.statistics.vo;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "商户端今日经营概览（全部口径为服务器时区当日，均不含已取消订单）")
public class StatisticsOverviewVO {

    @Schema(description = "今日有效订单数", example = "3")
    private Long todayOrderCount;

    @Schema(description = "今日有效营收（整数分）", example = "3668")
    private Long todayRevenue;

    @Schema(description = "今日各状态订单数量（覆盖全部 5 个状态，无数据的状态补零）")
    private List<StatusCountVO> statusDistribution;

    @Schema(description = "今日热销菜品 TOP N（默认 5，按销量降序）")
    private List<TopDishVO> topDishes;
}
