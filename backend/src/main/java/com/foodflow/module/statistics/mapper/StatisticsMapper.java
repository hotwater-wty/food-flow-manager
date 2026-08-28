package com.foodflow.module.statistics.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 统计查询 Mapper:只读聚合 SQL 集中在这里,不改动任何业务表数据。
 * 口径统一为"今日"(服务器时区当日)且排除已取消订单;热销榜从订单明细聚合。
 */
@Mapper
public interface StatisticsMapper {

    /**
     * 今日有效订单数与营收(分)
     */
    @Select("""
            SELECT COUNT(*) AS order_count, IFNULL(SUM(total_amount), 0) AS revenue
            FROM dining_order
            WHERE DATE(create_time) = CURDATE() AND status != 5
            """)
    Map<String, Object> selectTodaySummary();

    /**
     * 今日各状态订单数量(仅返回有数据的状态,缺失状态由服务层补零)
     */
    @Select("""
            SELECT status, COUNT(*) AS cnt
            FROM dining_order
            WHERE DATE(create_time) = CURDATE() AND status != 5
            GROUP BY status
            """)
    List<Map<String, Object>> selectTodayStatusCounts();

    /**
     * 今日热销菜品 TOP N(按数量降序,数量相同按金额降序)
     */
    @Select("""
            SELECT oi.dish_id AS dishId, oi.dish_name AS dishName,
                   SUM(oi.quantity) AS quantity, SUM(oi.amount) AS amount
            FROM order_item oi
            JOIN dining_order o ON oi.order_id = o.id
            WHERE DATE(o.create_time) = CURDATE() AND o.status != 5
            GROUP BY oi.dish_id, oi.dish_name
            ORDER BY quantity DESC, amount DESC
            LIMIT #{limit}
            """)
    List<Map<String, Object>> selectTodayTopDishes(int limit);
}
