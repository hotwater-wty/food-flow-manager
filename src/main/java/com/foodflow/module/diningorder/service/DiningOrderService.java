package com.foodflow.module.diningorder.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.foodflow.module.diningorder.dto.DiningOrderDTO;
import com.foodflow.module.diningorder.dto.OrderItemCreateDTO;
import com.foodflow.module.diningorder.entity.DiningOrder;
import com.foodflow.module.diningorder.vo.AdminDiningOrderDetailVO;
import com.foodflow.module.diningorder.vo.AdminDiningOrderVO;
import com.foodflow.module.diningorder.vo.DiningOrderCreateVO;
import com.foodflow.module.diningorder.vo.UserDiningOrderVO;

import jakarta.validation.Valid;

public interface DiningOrderService extends IService<DiningOrder> {

    DiningOrderCreateVO createOrder(Long sessionId, @Valid OrderItemCreateDTO orderItemsDTOList);

    List<UserDiningOrderVO> getOrderList(@Valid DiningOrderDTO diningOrderDTO);

    List<AdminDiningOrderVO> getAdminOrderList(@Valid DiningOrderDTO diningOrderDTO);

    AdminDiningOrderDetailVO getAdminOrderDetail(Long orderId);
    
}
