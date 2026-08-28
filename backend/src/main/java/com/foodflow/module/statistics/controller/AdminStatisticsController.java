package com.foodflow.module.statistics.controller;

import com.foodflow.common.result.Result;
import com.foodflow.module.statistics.service.StatisticsService;
import com.foodflow.module.statistics.vo.StatisticsOverviewVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商户端经营统计:店员与店长均可查看(与订单工作台同级权限),
 * 由 JwtTokenInterceptor 的 /api/admin/** 员工登录校验统一保护。
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/statistics")
@RequiredArgsConstructor
@Tag(name = "管理端-经营统计", description = "商户端今日经营概览统计接口")
public class AdminStatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 今日经营概览
     */
    @GetMapping("/overview")
    @Operation(summary = "查询今日经营概览", description = "今日有效订单数、营收（分）、状态分布与热销菜品 TOP5，均不含已取消订单")
    public Result<StatisticsOverviewVO> getTodayOverview() {
        log.info("管理员查询今日经营概览");
        return Result.success(statisticsService.getTodayOverview());
    }
}
