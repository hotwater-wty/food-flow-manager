package com.foodflow.module.reservation.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodflow.common.result.Result;
import com.foodflow.module.reservation.dto.ReservationDTO;
import com.foodflow.module.reservation.service.ReservationService;
import com.foodflow.module.reservation.vo.ReservationCreateVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/user/reservations")
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService reservationService;

    /**
     * 创建预约
     * 
     * @param reservationDTO
     * @return
     */
    @PostMapping
    public Result<ReservationCreateVO> createReservation(@RequestBody ReservationDTO reservationDTO){
        log.info("创建预约: {}", reservationDTO);
        ReservationCreateVO reservationCreateVO = reservationService.createReservation(reservationDTO);
        return Result.success(reservationCreateVO);
    }
}
