package com.foodflow.module.diningorder.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodflow.common.context.LoginContext;
import com.foodflow.common.enums.DiningSessionStatusEnum;
import com.foodflow.common.enums.DishStatusEnum;
import com.foodflow.common.enums.OrderStatusEnum;
import com.foodflow.common.exception.BusinessException;
import com.foodflow.common.utils.NumberUtils;
import com.foodflow.module.diningorder.dto.OrderItemDTO;
import com.foodflow.module.diningorder.entity.DiningOrder;
import com.foodflow.module.diningorder.mapper.DiningOrderMapper;
import com.foodflow.module.diningorder.service.DiningOrderService;
import com.foodflow.module.diningorder.vo.DiningOrderCreateVO;
import com.foodflow.module.diningsession.entity.DiningSession;
import com.foodflow.module.diningsession.service.DiningSessionService;
import com.foodflow.module.dish.entity.Dish;
import com.foodflow.module.dish.service.DishService;
import com.foodflow.module.orderitem.entity.OrderItem;
import com.foodflow.module.orderitem.service.OrderItemService;
import com.foodflow.module.orderitem.vo.OrderItemCreateVO;
import com.foodflow.module.table.service.DiningTableService;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DiningOrderServiceImpl extends ServiceImpl<DiningOrderMapper, DiningOrder> implements DiningOrderService {
    
    private final DiningSessionService diningSessionService;
    private final DishService dishService;
    private final DiningTableService diningTableService;
    private final OrderItemService orderItemService;
    @Override
    public DiningOrderCreateVO createOrder(Long sessionId, 
                List<OrderItemDTO> orderItemsDTOList) {
        DiningSession session = getExistingSession(sessionId);
        if(session.getUserId() != LoginContext.getUserId()) {
            throw new BusinessException("会话不属于当前用户");
        }
        if(session.getStatus() != DiningSessionStatusEnum.WAITING) {
            throw new BusinessException("会话状态错误");
        }

        // 获取菜品列表信息并校验
        List<Long> dishIds = orderItemsDTOList.stream()
                .map(OrderItemDTO::getDishId).toList();
        List<Dish> dishes = dishService.query()
                .in("dish_id", dishIds)
                .eq("status", DishStatusEnum.ON_SALE.getCode())
                .list();
        if(dishes.size() != dishIds.size()) {
            throw new BusinessException("菜品不存在或已下架");
        }

        // 组装菜品和对应菜品详情为Map
        Map<Dish, OrderItemDTO> dishAndDetailMap = new HashMap<>();
        for(int i = 0; i < dishes.size(); i++) {
            dishAndDetailMap.put(dishes.get(i), orderItemsDTOList.get(i));
        }

        // 计算总金额
        Integer totalAmount = calculateTotalAmount(dishAndDetailMap);

        LocalDateTime now = LocalDateTime.now();

        // 创建订单
        DiningOrder order = DiningOrder.builder()
                .orderNo(NumberUtils.generateOrderNo())
                .userId(LoginContext.getUserId())
                .tableId(session.getTableId())
                .sessionId(sessionId)
                .totalAmount(totalAmount)
                .status(OrderStatusEnum.PLACED)
                .createTime(now)
                .updateTime(now)
                .build();
        save(order);

        // 创建订单明细
        List<OrderItem> orderItems = new ArrayList<>();
        for(Map.Entry<Dish, OrderItemDTO> entry : dishAndDetailMap.entrySet()) {
            OrderItem orderItem = OrderItem.builder()
                    .orderId(order.getId())
                    .dishId(entry.getKey().getId())
                    .dishName(entry.getKey().getName())
                    .dishImage(entry.getKey().getImage())
                    .dishPrice(entry.getKey().getPrice())
                    .quantity(entry.getValue().getQuantity())
                    .amount(entry.getKey().getPrice() * entry.getValue().getQuantity())
                    .remark(entry.getValue().getRemark())
                    .createTime(now)
                    .updateTime(now)
                    .build();
            orderItems.add(orderItem);
        }
        orderItemService.saveBatch(orderItems);

        // 构建返回值
        return DiningOrderCreateVO.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .sessionId(sessionId)
                .tableId(session.getTableId())
                .tableNo(diningTableService.getById(session.getTableId()).getTableNo())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .items(itemsToVOList(orderItems))
                .build();
    }

    private DiningSession getExistingSession(Long sessionId) {
        DiningSession session = diningSessionService.getById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        return session;
    }

    private Integer calculateTotalAmount(Map<Dish, OrderItemDTO> dishAndDetailMap) {
        Integer totalAmount = 0;
        for(Map.Entry<Dish, OrderItemDTO> entry : dishAndDetailMap.entrySet()) {
            totalAmount += entry.getKey().getPrice() * entry.getValue().getQuantity();
        }
        return totalAmount;
    }

    private List<OrderItemCreateVO> itemsToVOList(List<OrderItem> orderItems) {
        return orderItems.stream().map(orderItem -> OrderItemCreateVO.builder()
                .dishId(orderItem.getDishId())
                .dishName(orderItem.getDishName())
                .dishPrice(orderItem.getDishPrice())
                .quantity(orderItem.getQuantity())
                .amount(orderItem.getAmount())
                .build()).toList();
    }
}
