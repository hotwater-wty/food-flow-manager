package com.foodflow.module.diningsession.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "用户端-堂食会话", description = "用户端当前堂食会话查询、预约到店和扫码开台接口")
public class UserDiningSessionController {

    private final DiningSessionService diningSessionService;

    /**
     * 用户查看当前开台会话
    */
    @GetMapping("/sessions/current")
    @Operation(summary = "查询当前堂食会话", description = "用户查询自己当前进行中的堂食会话")
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
    @Operation(summary = "预约到店开台", description = "预约用户扫码到店后，按预约ID和桌位ID创建堂食会话")
    public Result<DiningSessionVO> checkInReservation(
                @Parameter(description = "预约ID", example = "1") @PathVariable Long reservationId,
                @Parameter(description = "桌位ID", example = "1") @PathVariable Long tableId){
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
    @Operation(summary = "扫码开台", description = "非预约用户扫描桌位二维码后创建堂食会话")
    public Result<DiningSessionVO> checkInTable(
            @Parameter(description = "桌位ID", example = "1") @PathVariable Long tableId){
        log.info("非预约用户扫码占座: {}", tableId);
        DiningSessionVO diningSessionVO = diningSessionService
                .checkInTable(tableId);
        return Result.success(diningSessionVO);
    }
}
