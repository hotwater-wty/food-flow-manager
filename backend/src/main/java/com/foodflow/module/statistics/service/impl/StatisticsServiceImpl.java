package com.foodflow.module.statistics.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.foodflow.common.enums.OrderStatusEnum;
import com.foodflow.module.statistics.mapper.StatisticsMapper;
import com.foodflow.module.statistics.service.StatisticsService;
import com.foodflow.module.statistics.vo.StatisticsOverviewVO;
import com.foodflow.module.statistics.vo.StatusCountVO;
import com.foodflow.module.statistics.vo.TopDishVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    /** 热销榜单默认返回条数 */
    private static final int TOP_DISH_LIMIT = 5;

    private final StatisticsMapper statisticsMapper;

    /**
     * 查询今日经营概览：汇总数量/营收、补齐状态分布、裁剪热销榜。
     * 统计口径与 SQL 保持一致——服务器时区当日、排除已取消订单。
     */
    @Override
    public StatisticsOverviewVO getTodayOverview() {
        // 汇总 1：今日有效订单数与营收；Map 的数值来自 SQL 聚合，统一转 Long 避免类型歧义。
        Map<String, Object> summary = statisticsMapper.selectTodaySummary();

        // 汇总 2：状态分布按 5 个状态补零返回，前端不需要再处理"缺失状态"。
        List<StatusCountVO> distribution = new ArrayList<>();
        Map<Integer, Long> counts = new java.util.HashMap<>();
        for (Map<String, Object> row : statisticsMapper.selectTodayStatusCounts()) {
            Integer status = ((Number) row.get("status")).intValue();
            counts.put(status, ((Number) row.get("cnt")).longValue());
        }
        for (OrderStatusEnum status : OrderStatusEnum.values()) {
            distribution.add(StatusCountVO.builder()
                    .status(status.getCode())
                    .count(counts.getOrDefault(status.getCode(), 0L))
                    .build());
        }

        // 汇总 3：热销菜品,由 SQL 负责排序与条数限制。
        List<TopDishVO> topDishes = statisticsMapper.selectTodayTopDishes(TOP_DISH_LIMIT).stream()
                .map(row -> TopDishVO.builder()
                        .dishId(((Number) row.get("dishId")).longValue())
                        .dishName((String) row.get("dishName"))
                        .quantity(((Number) row.get("quantity")).longValue())
                        .amount(((Number) row.get("amount")).longValue())
                        .build())
                .toList();

        return StatisticsOverviewVO.builder()
                .todayOrderCount(((Number) summary.get("order_count")).longValue())
                .todayRevenue(((Number) summary.get("revenue")).longValue())
                .statusDistribution(distribution)
                .topDishes(topDishes)
                .build();
    }
}
