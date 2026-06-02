package com.foodflow.common.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NumberUtils {
    
    /**
     * 生成会话编号
     */
    public static String generateSessionOn() {
        return "Session" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    /**
     * 生成预约编号
     */
    public static String generateReservationNo() {
        // 使用时间戳和随机数生成预约编号
        // TODO 使用雪花算法等生成正式的预约编号，避免重复
        return "Reservation" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
}
