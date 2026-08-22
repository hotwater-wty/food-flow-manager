package com.foodflow.module.reservation.controller;

import com.foodflow.common.dto.PageQueryDTO;
import com.foodflow.common.result.PageResult;
import com.foodflow.common.result.Result;
import com.foodflow.module.reservation.service.ReservationService;
import com.foodflow.module.reservation.vo.ReservationVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/admin/reservations")
@Tag(name = "管理端-预约管理", description = "管理端预约列表、详情和异常取消接口")
public class AdminReservationController {
    private final ReservationService reservationService;

    /**
     * 获取所有预约列表
    */
    @GetMapping
    @Operation(summary = "查询全部预约列表", description = "管理端查询所有用户预约记录")
    public Result<PageResult<ReservationVO>> getReservationList(
            @ParameterObject @Validated PageQueryDTO pageQueryDTO){
        log.info("查看所有预约, pageQueryDTO: {}", pageQueryDTO);
        PageResult<ReservationVO> reservationList = reservationService.getAllReservation(pageQueryDTO);
        return Result.success(reservationList);
    }

    /**
     * 商户端取消异常预约
    */
    @PostMapping("/{reservationId}/cancel")
    @Operation(summary = "取消预约", description = "管理端取消异常预约")
    public Result<Void> cancelReservation(
            @Parameter(description = "预约ID", example = "1") @PathVariable Long reservationId){
        log.info("商户端取消异常预约: {}", reservationId);
        reservationService.cancelAdminReservation(reservationId);
        return Result.success();
    }

    /**
     * 查看预约详情
    */
    @GetMapping("/{reservationId}/detail")
    @Operation(summary = "查询预约详情", description = "管理端根据预约ID查询预约详情")
    public Result<ReservationVO> getReservationDetail(
            @Parameter(description = "预约ID", example = "1") @PathVariable Long reservationId){
        log.info("查看预约详情: {}", reservationId);
        ReservationVO reservationVO = reservationService.getAdminReservationDetail(reservationId);
        return Result.success(reservationVO);
    }
}
