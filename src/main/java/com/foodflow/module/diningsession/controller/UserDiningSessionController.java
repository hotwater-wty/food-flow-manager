package com.foodflow.module.diningsession.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodflow.common.result.Result;
import com.foodflow.module.diningsession.service.DiningSessionService;
import com.foodflow.module.diningsession.vo.DiningSessionVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserDiningSessionController {

    private final DiningSessionService diningSessionService;

    /**
     * 用户查看当前开台会话
     */
    @GetMapping("/sessions/current")
    public Result<DiningSessionVO> getCurrentSession() {
        log.info("用户获取当前开台");
        DiningSessionVO diningSessionVO = diningSessionService.getCurrentSession();
        return Result.success(diningSessionVO);
    }
    
    /**
     * 预约用户扫码到店
     * 
     * @param reservationId
     * @return
     */
    @PostMapping("/reservations/{reservationId}/check-in/{tableId}")
    public Result<DiningSessionVO> checkInReservation(
                @PathVariable Long reservationId, 
                @PathVariable Long tableId){
        log.info("预约用户扫码到店: {}", reservationId);
        DiningSessionVO diningSessionVO = diningSessionService
                .checkInReservation(reservationId, tableId);
        return Result.success(diningSessionVO);
    }

    /**
     * 非预约用户扫码占座
     * 
     * @param tableId
     * @return
     */
    @PostMapping("/tables/{tableId}/sessions")
    public Result<DiningSessionVO> checkInTable(@PathVariable Long tableId){
        log.info("非预约用户扫码占座: {}", tableId);
        DiningSessionVO diningSessionVO = diningSessionService
                .checkInTable(tableId);
        return Result.success(diningSessionVO);
    }
}
