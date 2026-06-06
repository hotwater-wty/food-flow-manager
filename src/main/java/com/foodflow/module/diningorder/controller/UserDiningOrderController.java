package com.foodflow.module.diningorder.controller;

import com.foodflow.common.result.Result;
import com.foodflow.module.diningorder.dto.OrderItemDTO;
import com.foodflow.module.diningorder.service.DiningOrderService;
import com.foodflow.module.diningorder.vo.DiningOrderCreateVO;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
                @Valid @RequestBody List<OrderItemDTO> orderItemsDTOList) {
        log.info("创建堂食订单, sessionId: {}, orderItemsDTOList: {}", 
                sessionId, orderItemsDTOList);
        DiningOrderCreateVO diningOrderCreateVO = diningOrderService
                .createOrder(sessionId, orderItemsDTOList);
        return Result.success(diningOrderCreateVO);
    }
}
