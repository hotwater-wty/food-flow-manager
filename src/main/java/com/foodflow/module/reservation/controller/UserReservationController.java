package com.foodflow.module.reservation.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodflow.common.result.Result;
import com.foodflow.module.diningsession.vo.DiningSessionVO;
import com.foodflow.module.reservation.dto.ReservationDTO;
import com.foodflow.module.reservation.service.ReservationService;
import com.foodflow.module.reservation.vo.ReservationCreateVO;
import com.foodflow.module.reservation.vo.ReservationVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/user/reservations")
@RequiredArgsConstructor
public class UserReservationController {
    private final ReservationService reservationService;

    /**
     * 创建预约
     * 
     * @param reservationDTO
     * @return
     */
    @PostMapping
    public Result<ReservationCreateVO> createReservation(@Validated @RequestBody ReservationDTO reservationDTO){
        log.info("创建预约: {}", reservationDTO);
        ReservationCreateVO reservationCreateVO = reservationService.createReservation(reservationDTO);
        return Result.success(reservationCreateVO);
    }

    /**
     * 用户查看预约列表
     * 
     * @return
     */
    @GetMapping
    public Result<List<ReservationVO>> getReservationList(){
        log.info("查看预约列表");
        List<ReservationVO> reservationList = reservationService.getReservation();
        return Result.success(reservationList);
    }

    /**
     * 查看预约详情
     */
    @GetMapping("/{reservationId}/detail")
    public Result<ReservationVO> getReservationDetail(@PathVariable Long reservationId){
        log.info("查看预约详情: {}", reservationId);
        ReservationVO reservationVO = reservationService.getUserReservationDetail(reservationId);
        return Result.success(reservationVO);
    }

    /**
     * 用户取消预约
     * 
     * @param reservationId
     * @return
     */
    @PostMapping("/{reservationId}/cancel")
    public Result<Void> cancelReservation(@PathVariable Long reservationId){
        log.info("用户取消预约: {}", reservationId);
        reservationService.cancelReservation(reservationId);
        return Result.success();
    }

    /**
     * 预约用户扫码到店
     * 
     * @param reservationId
     * @return
     */
    @PostMapping("/{reservationId}/check-in")
    public Result<DiningSessionVO> checkInReservation(@PathVariable Long reservationId){
        log.info("预约用户扫码到店: {}", reservationId);
        DiningSessionVO diningSessionVO = reservationService.checkInReservation(reservationId);
        return Result.success(diningSessionVO);
    }
}
