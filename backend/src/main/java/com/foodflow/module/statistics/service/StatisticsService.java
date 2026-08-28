package com.foodflow.module.statistics.service;

import com.foodflow.module.statistics.vo.StatisticsOverviewVO;

/**
 * 商户端经营统计服务
 */
public interface StatisticsService {

    /**
     * 查询今日经营概览
     * @return 统计数据
     */
    StatisticsOverviewVO getTodayOverview();
}
