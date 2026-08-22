package com.foodflow.module.orderitem.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodflow.module.orderitem.entity.OrderItem;
import com.foodflow.module.orderitem.mapper.OrderItemMapper;
import com.foodflow.module.orderitem.service.OrderItemService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem> implements OrderItemService {
    
}
