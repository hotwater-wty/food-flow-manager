package com.foodflow.module.diningorder.controller;

import com.foodflow.common.result.Result;
import com.foodflow.module.diningorder.dto.DiningOrderDTO;
import com.foodflow.module.diningorder.dto.OrderStatusUpdateDTO;
import com.foodflow.module.diningorder.service.DiningOrderService;
import com.foodflow.module.diningorder.vo.AdminDiningOrderDetailVO;
import com.foodflow.module.diningorder.vo.AdminDiningOrderVO;
import com.foodflow.module.diningorder.vo.DiningOrderUpdateVO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
public class AdminDiningOrderController {
    private final DiningOrderService diningOrderService;

    /**
     * 管理员查看堂食订单列表
     */
    @GetMapping
    public Result<List<AdminDiningOrderVO>> getOrderList(
            @Valid DiningOrderDTO diningOrderDTO) {
        log.info("管理员查看堂食订单列表, diningOrderDTO: {}", diningOrderDTO);
        List<AdminDiningOrderVO> diningOrderVOList = diningOrderService
                .getAdminOrderList(diningOrderDTO);
        return Result.success(diningOrderVOList);
    }

    /**
     * 管理员查看堂食订单详情
     */
    @GetMapping("/{orderId}")
    public Result<AdminDiningOrderDetailVO> getOrderDetail(
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
    public Result<DiningOrderUpdateVO> updateOrderStatus(
            @PathVariable @NotNull(message = "订单ID不能为空") Long orderId,
            @Valid @RequestBody OrderStatusUpdateDTO orderStatusUpdateDTO) {
        log.info("管理员更新堂食订单状态, orderId: {}, orderStatusUpdateDTO: {}", orderId, orderStatusUpdateDTO);
        DiningOrderUpdateVO diningOrderUpdateVO = diningOrderService
                .updateAdminOrderStatus(orderId, orderStatusUpdateDTO);
        return Result.success(diningOrderUpdateVO);
    }
}
