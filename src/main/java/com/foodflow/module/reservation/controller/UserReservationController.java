package com.foodflow.module.reservation.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodflow.common.context.LoginContext;
import com.foodflow.common.context.LoginInfo;
import com.foodflow.common.enums.SubmitSceneEnum;
import com.foodflow.common.result.Result;
import com.foodflow.common.service.SubmitTokenService;
import com.foodflow.module.reservation.dto.ReservationDTO;
import com.foodflow.module.reservation.service.ReservationService;
import com.foodflow.module.reservation.vo.ReservationCreateVO;
import com.foodflow.module.reservation.vo.ReservationVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/user/reservations")
@RequiredArgsConstructor
@Tag(name = "用户端-预约管理", description = "用户端创建、查询和取消预约接口")
public class UserReservationController {
    private final ReservationService reservationService;
    private final SubmitTokenService submitTokenService;

    /**
     * 创建预约
     * 
     * @param reservationDTO
     * @return
    */
    @PostMapping
    @Operation(summary = "创建预约", description = "用户选择空闲桌位、人数和预约时间创建预约")
    public Result<ReservationCreateVO> createReservation(
            @Parameter(description = "防重复提交token") @RequestHeader("X-Submit-Token") String submitToken,
            @Validated @RequestBody ReservationDTO reservationDTO){
                
        LoginInfo loginInfo = LoginContext.get();
        submitTokenService.validateAndConsume(
                loginInfo.getLoginType(),
                loginInfo.getUserId(),
                SubmitSceneEnum.CREATE_RESERVATION,
                submitToken);
                
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
    @Operation(summary = "查询我的预约列表", description = "用户查询自己的预约记录")
    public Result<List<ReservationVO>> getReservationList(){
        log.info("查看预约列表");
        List<ReservationVO> reservationList = reservationService.getReservation();
        return Result.success(reservationList);
    }

    /**
     * 查看预约详情
    */
    @GetMapping("/{reservationId}/detail")
    @Operation(summary = "查询我的预约详情", description = "用户根据预约ID查询自己的预约详情")
    public Result<ReservationVO> getReservationDetail(
            @Parameter(description = "预约ID", example = "1") @PathVariable Long reservationId){
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
    @Operation(summary = "取消我的预约", description = "用户取消自己的未到店预约")
    public Result<Void> cancelReservation(
            @Parameter(description = "预约ID", example = "1") @PathVariable Long reservationId){
        log.info("用户取消预约: {}", reservationId);
        reservationService.cancelReservation(reservationId);
        return Result.success();
    }

    
}
