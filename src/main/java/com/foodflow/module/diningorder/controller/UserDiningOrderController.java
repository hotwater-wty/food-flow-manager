package com.foodflow.module.diningorder.controller;

import com.foodflow.common.result.Result;
import com.foodflow.module.diningorder.dto.OrderItemCreateDTO;
import com.foodflow.module.diningorder.service.DiningOrderService;
import com.foodflow.module.diningorder.vo.DiningOrderCreateVO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
}
