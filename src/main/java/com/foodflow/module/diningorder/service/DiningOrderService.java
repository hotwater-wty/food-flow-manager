package com.foodflow.module.diningorder.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.foodflow.module.diningorder.dto.OrderItemDTO;
import com.foodflow.module.diningorder.entity.DiningOrder;
import com.foodflow.module.diningorder.vo.DiningOrderCreateVO;

import jakarta.validation.Valid;

public interface DiningOrderService extends IService<DiningOrder> {

    DiningOrderCreateVO createOrder(Long sessionId, @Valid List<OrderItemDTO> orderItemsDTOList);
    
}
