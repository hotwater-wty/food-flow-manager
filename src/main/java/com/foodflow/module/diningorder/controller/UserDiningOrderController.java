package com.foodflow.module.diningorder.controller;

import com.foodflow.common.result.Result;
import com.foodflow.module.diningorder.dto.DiningOrderDTO;
import com.foodflow.module.diningorder.dto.OrderItemCreateDTO;
import com.foodflow.module.diningorder.service.DiningOrderService;
import com.foodflow.module.diningorder.vo.DiningOrderCreateVO;
import com.foodflow.module.diningorder.vo.UserDiningOrderDetailVO;
import com.foodflow.module.diningorder.vo.UserDiningOrderVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "用户端-堂食订单", description = "用户端堂食下单、订单列表和订单详情接口")
public class UserDiningOrderController {
    private final DiningOrderService diningOrderService;
    
    /**
     * 创建堂食订单
    */
    @PostMapping("/sessions/{sessionId}/orders")
    @Operation(summary = "创建堂食订单", description = "用户在当前堂食会话中提交菜品明细并创建订单")
    public Result<DiningOrderCreateVO> createOrder(
                @Parameter(description = "堂食会话ID", example = "1") @PathVariable Long sessionId,
                @Valid @RequestBody OrderItemCreateDTO orderItemCreateDTO) {
        log.info("创建堂食订单, sessionId: {}, orderItemsDTOList: {}", 
                sessionId, orderItemCreateDTO);
        DiningOrderCreateVO diningOrderCreateVO = diningOrderService
                .createOrder(sessionId, orderItemCreateDTO);
        return Result.success(diningOrderCreateVO);
    }

    /**
     * 用户查看堂食订单列表
    */
    @GetMapping("/orders")
    @Operation(summary = "查询我的堂食订单列表", description = "用户按桌位、订单ID或订单状态筛选自己的堂食订单")
    public Result<List<UserDiningOrderVO>> getOrderList(
                @ParameterObject @Valid DiningOrderDTO diningOrderDTO) {
        log.info("用户查看堂食订单列表, diningOrderDTO: {}", diningOrderDTO);
        List<UserDiningOrderVO> diningOrderVOList = diningOrderService
                .getOrderList(diningOrderDTO);
        return Result.success(diningOrderVOList);
    }

    /**
     * 用户查看堂食订单详情
    */
    @GetMapping("/orders/{orderId}")
    @Operation(summary = "查询我的堂食订单详情", description = "用户根据订单ID查询自己的堂食订单详情和明细")
    public Result<UserDiningOrderDetailVO> getOrderDetail(
            @Parameter(description = "订单ID", example = "1") @PathVariable Long orderId) {
        log.info("用户查看堂食订单详情, orderId: {}", orderId);
        UserDiningOrderDetailVO diningOrderVO = diningOrderService
                .getOrderDetail(orderId);
        return Result.success(diningOrderVO);
    }
}
