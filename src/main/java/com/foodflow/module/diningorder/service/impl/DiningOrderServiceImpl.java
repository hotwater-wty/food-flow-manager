package com.foodflow.module.diningorder.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodflow.common.context.LoginContext;
import com.foodflow.common.enums.DiningSessionStatusEnum;
import com.foodflow.common.enums.DishStatusEnum;
import com.foodflow.common.enums.OrderStatusEnum;
import com.foodflow.common.enums.TableStatusEnum;
import com.foodflow.common.exception.BusinessException;
import com.foodflow.common.utils.NumberUtils;
import com.foodflow.module.diningorder.dto.DiningOrderDTO;
import com.foodflow.module.diningorder.dto.OrderItemCreateDTO;
import com.foodflow.module.diningorder.dto.OrderItemDTO;
import com.foodflow.module.diningorder.entity.DiningOrder;
import com.foodflow.module.diningorder.mapper.DiningOrderMapper;
import com.foodflow.module.diningorder.service.DiningOrderService;
import com.foodflow.module.diningorder.vo.AdminDiningOrderDetailVO;
import com.foodflow.module.diningorder.vo.AdminDiningOrderVO;
import com.foodflow.module.diningorder.vo.DiningOrderCreateVO;
import com.foodflow.module.diningorder.vo.UserDiningOrderVO;
import com.foodflow.module.diningsession.entity.DiningSession;
import com.foodflow.module.diningsession.service.DiningSessionService;
import com.foodflow.module.dish.entity.Dish;
import com.foodflow.module.dish.service.DishService;
import com.foodflow.module.orderitem.entity.OrderItem;
import com.foodflow.module.orderitem.service.OrderItemService;
import com.foodflow.module.orderitem.vo.OrderItemCreateVO;
import com.foodflow.module.orderitem.vo.OrderItemVO;
import com.foodflow.module.table.entity.DiningTable;
import com.foodflow.module.table.service.DiningTableService;

import io.jsonwebtoken.lang.Collections;
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
                .items(itemsToCreateVOList(orderItems))
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

    private List<OrderItemCreateVO> itemsToCreateVOList(List<OrderItem> orderItems) {
        if (orderItems.isEmpty()) {
            return Collections.emptyList();
        }
        return orderItems.stream().map(orderItem -> OrderItemCreateVO.builder()
                .dishId(orderItem.getDishId())
                .dishName(orderItem.getDishName())
                .dishPrice(orderItem.getDishPrice())
                .quantity(orderItem.getQuantity())
                .amount(orderItem.getAmount())
                .build()).toList();
    }

    @Override
    public List<UserDiningOrderVO> getOrderList(DiningOrderDTO diningOrderDTO) {
        List<DiningOrder> orderList = query()
                .eq(diningOrderDTO.getTableId() != null, "table_id", diningOrderDTO.getTableId())
                .eq(diningOrderDTO.getOrderId() != null, "id", diningOrderDTO.getOrderId())
                .eq(diningOrderDTO.getStatusEnum() != null, "status", diningOrderDTO.getStatusEnum())
                .eq("user_id", LoginContext.getUserId()).list();
        if (orderList.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> tableIdList = orderList.stream()
                .map(DiningOrder::getTableId)
                .collect(Collectors.toList());
        List<DiningTable> tableList = diningTableService.listByIds(tableIdList);
        Map<Long, DiningTable> tableMap = tableList.stream()
                .collect(Collectors.toMap(DiningTable::getId, Function.identity()));
        return orderList.stream()
                .map(order -> UserDiningOrderVO.builder()
                        .orderId(order.getId())
                        .orderNo(order.getOrderNo())
                        .tableId(order.getTableId())
                        .tableNo(tableMap.get(order.getTableId()) == null 
                                ? "已删除桌位" 
                                : tableMap.get(order.getTableId()).getTableNo())
                        .totalAmount(order.getTotalAmount())
                        .status(order.getStatus().getCode())
                        .createTime(order.getCreateTime())
                        .build())
                .toList();
    }

    @Override
    public List<AdminDiningOrderVO> getAdminOrderList(DiningOrderDTO diningOrderDTO) {
        List<DiningOrder> orderList = query()
                .eq(diningOrderDTO.getTableId() != null, "table_id", diningOrderDTO.getTableId())
                .eq(diningOrderDTO.getOrderId() != null, "id", diningOrderDTO.getOrderId())
                .eq(diningOrderDTO.getStatusEnum() != null, "status", diningOrderDTO.getStatusEnum())
                .list();
        if (orderList.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> tableIdList = orderList.stream()
                .map(DiningOrder::getTableId)
                .collect(Collectors.toList());
        List<DiningTable> tableList = diningTableService.listByIds(tableIdList);
        Map<Long, DiningTable> tableMap = tableList.stream()
                .collect(Collectors.toMap(DiningTable::getId, Function.identity()));
        return orderList.stream()
                .map(order -> AdminDiningOrderVO.builder()
                        .orderId(order.getId())
                        .orderNo(order.getOrderNo())
                        .sessionId(order.getSessionId())
                        .tableId(order.getTableId())
                        .tableNo(tableMap.get(order.getTableId()) == null 
                                ? "已删除桌位" 
                                : tableMap.get(order.getTableId()).getTableNo())
                        .totalAmount(order.getTotalAmount())
                        .status(order.getStatus().getCode())
                        .createTime(order.getCreateTime())
                        .build())
                .toList();
    }

    @Override
    public AdminDiningOrderDetailVO getAdminOrderDetail(Long orderId) {
        DiningOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        DiningTable table = diningTableService.getById(order.getTableId());
        if (order.getStatus() != OrderStatusEnum.COMPLETED
                && order.getStatus() != OrderStatusEnum.CANCELED
                && table == null) {
            throw new BusinessException("桌位不存在，状态异常");
        }
        List<OrderItem> orderItemList = orderItemService.query()
                .eq("order_id", orderId)
                .list();
        return AdminDiningOrderDetailVO.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .sessionId(order.getSessionId())
                .tableId(order.getTableId())
                .tableNo(table == null 
                        ? "已删除桌位" 
                        : table.getTableNo())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().getCode())
                .createTime(order.getCreateTime())
                .items(itemsToVOList(orderItemList))
                .build();
    }

    private List<OrderItemVO> itemsToVOList(List<OrderItem> orderItems) {
        if (orderItems.isEmpty()) {
            return Collections.emptyList();
        }
        return orderItems.stream().map(orderItem -> OrderItemVO.builder()
                .dishId(orderItem.getDishId())
                .dishName(orderItem.getDishName())
                .dishPrice(orderItem.getDishPrice())
                .quantity(orderItem.getQuantity())
                .amount(orderItem.getAmount())
                .remark(orderItem.getRemark())
                .build()).toList();
    }
}
