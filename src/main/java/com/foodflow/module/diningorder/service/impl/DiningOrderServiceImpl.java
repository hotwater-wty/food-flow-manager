package com.foodflow.module.diningorder.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodflow.common.context.LoginContext;
import com.foodflow.common.enums.DiningSessionStatusEnum;
import com.foodflow.common.enums.DishStatusEnum;
import com.foodflow.common.enums.OrderStatusEnum;
import com.foodflow.common.enums.TableStatusEnum;
import com.foodflow.common.exception.BusinessException;
import com.foodflow.common.utils.NumberUtils;
import com.foodflow.module.diningorder.dto.OrderItemCreateDTO;
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
import com.foodflow.module.table.entity.DiningTable;
import com.foodflow.module.table.service.DiningTableService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiningOrderServiceImpl extends ServiceImpl<DiningOrderMapper, DiningOrder> implements DiningOrderService {

    private final DiningSessionService diningSessionService;
    private final DishService dishService;
    private final DiningTableService diningTableService;
    private final OrderItemService orderItemService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DiningOrderCreateVO createOrder(Long sessionId, OrderItemCreateDTO orderItemCreateDTO) {
        DiningSession session = diningSessionService.getById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        if (!Objects.equals(session.getUserId(), LoginContext.getUserId())) {
            throw new BusinessException("会话不属于当前用户");
        }
        if (session.getStatus() != DiningSessionStatusEnum.WAITING) {
            throw new BusinessException("会话状态错误");
        }

        List<OrderItemDTO> items = orderItemCreateDTO.getItems();

        List<Long> dishIds = items.stream()
                .map(OrderItemDTO::getDishId)
                .distinct()
                .toList();
        List<Dish> dishes = dishService.query()
                .in("id", dishIds)
                .eq("status", DishStatusEnum.ON_SALE.getCode())
                .list();
        if (dishes.size() != dishIds.size()) {
            throw new BusinessException("菜品不存在或已下架");
        }

        Map<Long, Dish> dishMap = dishes.stream()
                .collect(Collectors.toMap(Dish::getId, Function.identity()));
        Integer totalAmount = calculateTotalAmount(items, dishMap);

        LocalDateTime now = LocalDateTime.now();

        session.setStatus(DiningSessionStatusEnum.DINING);
        session.setFirstOrderTime(now);
        session.setUpdateTime(now);
        diningSessionService.updateById(session);

        DiningTable diningTable = diningTableService.getById(session.getTableId());
        if (diningTable == null) {
            throw new BusinessException("桌位不存在");
        }
        if (!Objects.equals(diningTable.getCurrentSessionId(), sessionId)) {
            throw new BusinessException("桌位与会话匹配异常");
        }
        diningTable.setStatus(TableStatusEnum.DINING);
        diningTable.setUpdateTime(now);
        diningTableService.updateById(diningTable);

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

        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemDTO item : items) {
            Dish dish = dishMap.get(item.getDishId());
            OrderItem orderItem = OrderItem.builder()
                    .orderId(order.getId())
                    .dishId(dish.getId())
                    .dishName(dish.getName())
                    .dishImage(dish.getImage())
                    .dishPrice(dish.getPrice())
                    .quantity(item.getQuantity())
                    .amount(dish.getPrice() * item.getQuantity())
                    .remark(item.getRemark())
                    .createTime(now)
                    .updateTime(now)
                    .build();
            orderItems.add(orderItem);
        }
        orderItemService.saveBatch(orderItems);

        return DiningOrderCreateVO.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .sessionId(sessionId)
                .tableId(session.getTableId())
                .tableNo(diningTable.getTableNo())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().getCode())
                .items(itemsToVOList(orderItems))
                .build();
    }

    /* private DiningSession getExistingSession(Long sessionId) {
        DiningSession session = diningSessionService.getById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        return session;
    } */

    private Integer calculateTotalAmount(List<OrderItemDTO> items, Map<Long, Dish> dishMap) {
        Integer totalAmount = 0;
        for (OrderItemDTO item : items) {
            Dish dish = dishMap.get(item.getDishId());
            totalAmount += dish.getPrice() * item.getQuantity();
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
