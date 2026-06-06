package com.foodflow.module.diningorder.controller;

import com.foodflow.common.result.Result;
import com.foodflow.module.diningorder.dto.DiningOrderDTO;
import com.foodflow.module.diningorder.service.DiningOrderService;
import com.foodflow.module.diningorder.vo.AdminDiningOrderVO;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
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
}
