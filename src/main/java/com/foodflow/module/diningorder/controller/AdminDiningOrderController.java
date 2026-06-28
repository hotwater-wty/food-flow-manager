package com.foodflow.module.diningorder.controller;

import com.foodflow.common.result.PageResult;
import com.foodflow.common.result.Result;
import com.foodflow.module.diningorder.dto.DiningOrderDTO;
import com.foodflow.module.diningorder.dto.OrderStatusUpdateDTO;
import com.foodflow.module.diningorder.service.DiningOrderService;
import com.foodflow.module.diningorder.vo.AdminDiningOrderDetailVO;
import com.foodflow.module.diningorder.vo.AdminDiningOrderVO;
import com.foodflow.module.diningorder.vo.DiningOrderUpdateVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@Tag(name = "管理端-堂食订单管理", description = "管理端堂食订单列表、详情和状态流转接口")
public class AdminDiningOrderController {
    private final DiningOrderService diningOrderService;

    /**
     * 管理员查看堂食订单列表
    */
    @GetMapping
    @Operation(summary = "查询堂食订单列表", description = "管理端按桌位、订单ID或订单状态筛选堂食订单")
    public Result<PageResult<AdminDiningOrderVO>> getOrderList(
            @ParameterObject @Valid DiningOrderDTO diningOrderDTO) {
        log.info("管理员查看堂食订单列表, diningOrderDTO: {}", diningOrderDTO);
        PageResult<AdminDiningOrderVO> diningOrderVOList = diningOrderService
                .getAdminOrderList(diningOrderDTO);
        return Result.success(diningOrderVOList);
    }

    /**
     * 管理员查看堂食订单详情
    */
    @GetMapping("/{orderId}")
    @Operation(summary = "查询堂食订单详情", description = "管理端根据订单ID查询堂食订单详情和明细")
    public Result<AdminDiningOrderDetailVO> getOrderDetail(
            @Parameter(description = "订单ID", example = "1")
            @PathVariable @NotNull(message = "订单ID不能为空") Long orderId) {
        log.info("管理员查看堂食订单详情, orderId: {}", orderId);
        AdminDiningOrderDetailVO diningOrderDetailVO = diningOrderService
                .getAdminOrderDetail(orderId);
        return Result.success(diningOrderDetailVO);
    }

    /**
     * 管理员更新堂食订单状态
    */
    @PutMapping("/{orderId}/status")
    @Operation(summary = "修改堂食订单状态", description = "管理端更新堂食订单状态，用于接单、制作、完成等状态流转")
    public Result<DiningOrderUpdateVO> updateOrderStatus(
            @Parameter(description = "订单ID", example = "1")
            @PathVariable @NotNull(message = "订单ID不能为空") Long orderId,
            @Valid @RequestBody OrderStatusUpdateDTO orderStatusUpdateDTO) {
        log.info("管理员更新堂食订单状态, orderId: {}, orderStatusUpdateDTO: {}", orderId, orderStatusUpdateDTO);
        DiningOrderUpdateVO diningOrderUpdateVO = diningOrderService
                .updateAdminOrderStatus(orderId, orderStatusUpdateDTO);
        return Result.success(diningOrderUpdateVO);
    }
    
}
