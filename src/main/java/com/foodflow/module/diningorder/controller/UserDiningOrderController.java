package com.foodflow.module.diningorder.controller;

import com.foodflow.common.result.Result;
import com.foodflow.module.diningorder.dto.DiningOrderDTO;
import com.foodflow.module.diningorder.dto.OrderItemCreateDTO;
import com.foodflow.module.diningorder.service.DiningOrderService;
import com.foodflow.module.diningorder.vo.DiningOrderCreateVO;
import com.foodflow.module.diningorder.vo.UserDiningOrderVO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

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
public class UserDiningOrderController {
    private final DiningOrderService diningOrderService;
    
    /**
     * 创建堂食订单
     */
    @PostMapping("/sessions/{sessionId}/orders")
    public Result<DiningOrderCreateVO> createOrder(
                @PathVariable Long sessionId,
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
    public Result<List<UserDiningOrderVO>> getOrderList(
                @Valid DiningOrderDTO diningOrderDTO) {
        log.info("用户查看堂食订单列表, diningOrderDTO: {}", diningOrderDTO);
        List<UserDiningOrderVO> diningOrderVOList = diningOrderService
                .getOrderList(diningOrderDTO);
        return Result.success(diningOrderVOList);
    }
}
