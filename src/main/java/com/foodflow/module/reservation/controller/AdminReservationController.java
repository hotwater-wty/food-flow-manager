package com.foodflow.module.reservation.controller;

import com.foodflow.common.result.Result;
import com.foodflow.module.reservation.service.ReservationService;
import com.foodflow.module.reservation.vo.ReservationVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/admin/reservations")
public class AdminReservationController {
    private final ReservationService reservationService;

    /**
     * 获取所有预约列表
     */
    @GetMapping
    public Result<List<ReservationVO>> getReservationList(){
        log.info("查看所有预约");
        List<ReservationVO> reservationList = reservationService.getAllReservation();
        return Result.success(reservationList);
    }

    /**
     * 商户端取消异常预约
     */
    @PostMapping("/{reservationId}/cancel")
    public Result<Void> cancelReservation(@PathVariable Long reservationId){
        log.info("商户端取消异常预约: {}", reservationId);
        reservationService.cancelAdminReservation(reservationId);
        return Result.success();
    }

    /**
     * 查看预约详情
     */
    @GetMapping("/{reservationId}/detail")
    public Result<ReservationVO> getReservationDetail(@PathVariable Long reservationId){
        log.info("查看预约详情: {}", reservationId);
        ReservationVO reservationVO = reservationService.getAdminReservationDetail(reservationId);
        return Result.success(reservationVO);
    }
}
