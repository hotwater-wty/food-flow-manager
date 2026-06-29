package com.foodflow.module.diningorder.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodflow.common.context.LoginContext;
import com.foodflow.common.dto.PageQueryDTO;
import com.foodflow.common.enums.DiningSessionStatusEnum;
import com.foodflow.common.enums.DishStatusEnum;
import com.foodflow.common.enums.OrderStatusEnum;
import com.foodflow.common.enums.TableStatusEnum;
import com.foodflow.common.exception.BusinessException;
import com.foodflow.common.result.PageResult;
import com.foodflow.common.utils.NumberUtils;
import com.foodflow.module.diningorder.dto.DiningOrderDTO;
import com.foodflow.module.diningorder.dto.OrderItemCreateDTO;
import com.foodflow.module.diningorder.dto.OrderItemDTO;
import com.foodflow.module.diningorder.dto.OrderStatusUpdateDTO;
import com.foodflow.module.diningorder.entity.DiningOrder;
import com.foodflow.module.diningorder.mapper.DiningOrderMapper;
import com.foodflow.module.diningorder.service.DiningOrderService;
import com.foodflow.module.diningorder.vo.AdminDiningOrderDetailVO;
import com.foodflow.module.diningorder.vo.AdminDiningOrderVO;
import com.foodflow.module.diningorder.vo.DiningOrderCreateVO;
import com.foodflow.module.diningorder.vo.DiningOrderUpdateVO;
import com.foodflow.module.diningorder.vo.UserDiningOrderDetailVO;
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

import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
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
    private final RedisTemplate<String, Object> redisTemplate;

    

    /**
     * 创建用餐订单
     * 
     * @param sessionId 用餐话话ID
     * @param orderItemCreateDTO 订单项创建DTO
     * @return 订单创建VO
     */
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

        // 获取订单项列表
        List<OrderItemDTO> items = orderItemCreateDTO.getItems();

        // 获取订单项中的菜品ID列表
        List<Long> dishIds = items.stream()
                .map(OrderItemDTO::getDishId)
                .distinct()
                .toList();
        
        // 查询订单项中的菜品
        List<Dish> dishes = dishService.lambdaQuery()
                .in(Dish::getId, dishIds)
                .eq(Dish::getStatus, DishStatusEnum.ON_SALE)
                .list();
        if (dishes.size() != dishIds.size()) {
            throw new BusinessException("菜品不存在或已下架");
        }

        // 构建菜品ID到菜品的映射
        Map<Long, Dish> dishMap = dishes.stream()
                .collect(Collectors.toMap(Dish::getId, Function.identity()));
        
        // 计算订单总金额
        Integer totalAmount = calculateTotalAmount(items, dishMap);

        // 桌位状态审查
        DiningTable diningTable = diningTableService.getById(session.getTableId());
        if (diningTable == null) {
            throw new BusinessException("桌位不存在");
        }
        if (diningTable.getStatus() != TableStatusEnum.WAITING) {
            throw new BusinessException("桌位状态错误");
        }
        if (!Objects.equals(diningTable.getCurrentSessionId(), sessionId)) {
            throw new BusinessException("桌位与会话匹配异常");
        }

        LocalDateTime now = LocalDateTime.now();

        // 更新会话状态
        boolean sessionUpdated = diningSessionService.lambdaUpdate()
                .eq(DiningSession::getId, sessionId)
                .eq(DiningSession::getUserId, LoginContext.getUserId())
                .eq(DiningSession::getTableId, session.getTableId())
                .eq(DiningSession::getStatus, DiningSessionStatusEnum.WAITING)
                .set(DiningSession::getStatus, DiningSessionStatusEnum.DINING)
                .set(DiningSession::getFirstOrderTime, now)
                .set(DiningSession::getUpdateTime, now)
                .update();
        if (!sessionUpdated) {
            throw new BusinessException("会话状态更新失败");
        }

        // 更新桌位状态
        boolean tableUpdated = diningTableService.lambdaUpdate()
                        .eq(DiningTable::getId, session.getTableId())
                        .eq(DiningTable::getCurrentSessionId, sessionId)
                        .eq(DiningTable::getStatus, TableStatusEnum.WAITING)
                        .set(DiningTable::getStatus, TableStatusEnum.DINING)
                        .set(DiningTable::getUpdateTime, now)
                        .update();
        if (!tableUpdated) {
            throw new BusinessException("桌位状态更新失败");
        }

        // TODO 此处操作缓存

        // 获取更新后的桌位状态
        diningTable = diningTableService.getById(session.getTableId());
        session = diningSessionService.getById(sessionId);

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
        // 保存订单
        save(order);

        // 创建订单项
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
        // 保存订单项
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
    public UserDiningOrderDetailVO getOrderDetail(Long orderId) {
        DiningOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!Objects.equals(order.getUserId(), LoginContext.getUserId())) {
            throw new BusinessException("只能查看自己的订单");
        }
        DiningTable table = diningTableService.getById(order.getTableId());
        List<OrderItem> orderItemList = orderItemService.query()
                .eq("order_id", orderId)
                .list();
        return toUserDiningOrderDetailVO(order, table, orderItemList);
    }

    private UserDiningOrderDetailVO toUserDiningOrderDetailVO(
            DiningOrder order, DiningTable table, List<OrderItem> orderItemList) {
        return UserDiningOrderDetailVO.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .tableId(order.getTableId())
                .tableNo(table == null ? "已删除桌位" : table.getTableNo())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().getCode())
                .createTime(order.getCreateTime())
                .items(itemsToVOList(orderItemList))
                .build();
    }

    /**
     * 管理员获取订单列表
     * 
     * @param diningOrderDTO 订单查询参数
     * @return 订单列表
     */
    @Override
    public PageResult<AdminDiningOrderVO> getAdminOrderList(DiningOrderDTO diningOrderDTO) {
        // 引入MyBatis-Plus分页查询功能
        Page<DiningOrder> page = new Page<>(diningOrderDTO.getPageNo(), diningOrderDTO.getPageSize());
        // 分页查询订单
        Page<DiningOrder> orderPage = page(page, lambdaQuery()
                .eq(diningOrderDTO.getTableId() != null, DiningOrder::getTableId, diningOrderDTO.getTableId())
                .eq(diningOrderDTO.getOrderId() != null, DiningOrder::getId, diningOrderDTO.getOrderId())
                .eq(diningOrderDTO.getStatusEnum() != null, DiningOrder::getStatus, diningOrderDTO.getStatusEnum())
                .orderByDesc(DiningOrder::getCreateTime)
                .getWrapper());
        // 处理分页结果
        List<DiningOrder> orderList = orderPage.getRecords();
        
        if (orderList.isEmpty()) {
            return new PageResult<>(
                    orderPage.getTotal(),
                    diningOrderDTO.getPageNo(),
                    diningOrderDTO.getPageSize(),
                    Collections.emptyList()
            );
        }

        // 获取桌位ID列表
        List<Long> tableIds = orderList.stream()
                .map(DiningOrder::getTableId)
                .distinct()
                .toList();
        // 处理订单列表，生成桌位信息的id映射表
        Map<Long, DiningTable> tableMap = diningTableService.listByIds(tableIds)
                .stream()
                .collect(Collectors.toMap(DiningTable::getId, Function.identity()));
        // 封装订单列表
        List<AdminDiningOrderVO> records = orderList.stream()
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
        return new PageResult<>(
                orderPage.getTotal(),
                diningOrderDTO.getPageNo(),
                diningOrderDTO.getPageSize(),
                records
        );
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DiningOrderUpdateVO updateAdminOrderStatus(Long orderId, OrderStatusUpdateDTO orderStatusUpdateDTO) {
        DiningOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        OrderStatusEnum currentStatus = order.getStatus();
        if (currentStatus == OrderStatusEnum.COMPLETED 
                || currentStatus == OrderStatusEnum.CANCELED) {
            throw new BusinessException("订单已完成或已取消，不能更新状态");
        }

        OrderStatusEnum newStatus = OrderStatusEnum.ofCode(orderStatusUpdateDTO.getStatus());
        if (newStatus == null) {
            throw new BusinessException("订单状态不能为空");
        }
        if (newStatus == OrderStatusEnum.CANCELED) {
            throw new BusinessException("暂不支持取消订单");
        }
        if (newStatus == currentStatus) {
            // 幂等返回当前状态
            return DiningOrderUpdateVO.builder()
                    .orderId(order.getId())
                    .orderNo(order.getOrderNo())
                    .status(order.getStatus().getCode())
                    .build();
        }
        if (newStatus.getCode() != currentStatus.getCode() + 1) {
            throw new BusinessException("订单状态流转不合法");
        }

        DiningTable table = diningTableService.getById(order.getTableId());
        if (table == null) {
            throw new BusinessException("桌位不存在");
        }
        DiningSession session = diningSessionService.getById(order.getSessionId());
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        
        order.setStatus(newStatus);
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);
        return DiningOrderUpdateVO.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .status(order.getStatus().getCode())
                .build();
    }

}
