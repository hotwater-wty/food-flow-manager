package com.foodflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.foodflow.common.context.LoginContext;
import com.foodflow.common.context.LoginInfo;
import com.foodflow.common.enums.ActiveFlagEnum;
import com.foodflow.common.enums.DiningSessionStatusEnum;
import com.foodflow.common.enums.EmployeeRoleEnum;
import com.foodflow.common.enums.LoginTypeEnum;
import com.foodflow.common.enums.OrderStatusEnum;
import com.foodflow.common.enums.ReservationStatusEnum;
import com.foodflow.common.enums.TableStatusEnum;
import com.foodflow.common.enums.UserStatusEnum;
import com.foodflow.common.exception.BusinessErrorCode;
import com.foodflow.common.exception.BusinessException;
import com.foodflow.module.diningorder.dto.OrderItemCreateDTO;
import com.foodflow.module.diningorder.dto.OrderItemDTO;
import com.foodflow.module.diningorder.dto.OrderStatusUpdateDTO;
import com.foodflow.module.diningorder.entity.DiningOrder;
import com.foodflow.module.diningorder.mapper.DiningOrderMapper;
import com.foodflow.module.diningorder.service.DiningOrderService;
import com.foodflow.module.diningorder.vo.DiningOrderCreateVO;
import com.foodflow.module.diningorder.vo.DiningOrderUpdateVO;
import com.foodflow.module.diningsession.entity.DiningSession;
import com.foodflow.module.diningsession.mapper.DiningSessionMapper;
import com.foodflow.module.diningsession.service.DiningSessionService;
import com.foodflow.module.diningsession.vo.DiningSessionCloseVO;
import com.foodflow.module.diningsession.vo.DiningSessionVO;
import com.foodflow.module.dish.entity.Dish;
import com.foodflow.module.dish.mapper.DishMapper;
import com.foodflow.module.employee.entity.Employee;
import com.foodflow.module.employee.mapper.EmployeeMapper;
import com.foodflow.module.orderitem.entity.OrderItem;
import com.foodflow.module.orderitem.mapper.OrderItemMapper;
import com.foodflow.module.reservation.dto.ReservationDTO;
import com.foodflow.module.reservation.entity.Reservation;
import com.foodflow.module.reservation.mapper.ReservationMapper;
import com.foodflow.module.reservation.service.ReservationService;
import com.foodflow.module.reservation.vo.ReservationCreateVO;
import com.foodflow.module.table.entity.DiningTable;
import com.foodflow.module.table.mapper.DiningTableMapper;
import com.foodflow.module.user.entity.User;
import com.foodflow.module.user.mapper.UserMapper;
import com.foodflow.testsupport.IntegrationTestContainers;

/**
 * P0-4 核心状态机回归：预约、下单、订单推进和清台均走真实 Service 与数据库。
 */
@Transactional
class CoreStateMachineIntegrationTest extends IntegrationTestContainers {
    private static final AtomicInteger DATA_SEQUENCE = new AtomicInteger();

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private DiningSessionService diningSessionService;

    @Autowired
    private DiningOrderService diningOrderService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private DiningTableMapper diningTableMapper;

    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private DiningSessionMapper diningSessionMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DiningOrderMapper diningOrderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @AfterEach
    void clearLoginContext() {
        LoginContext.clear();
    }

    @Test
    void reservationCreationAndCancellationReleaseTable() {
        User user = insertUser();
        DiningTable table = insertFreeTable();
        LoginContext.set(userLogin(user.getId()));

        ReservationDTO request = new ReservationDTO();
        request.setTableId(table.getId());
        request.setPeopleCount(2);
        request.setReserveTime(LocalDateTime.now().plusHours(2));

        ReservationCreateVO created = reservationService.createReservation(request);

        assertThat(created.getStatus()).isEqualTo(ReservationStatusEnum.WAITING_CHECK_IN.getCode());
        assertThat(diningTableMapper.selectById(table.getId()).getStatus())
                .isEqualTo(TableStatusEnum.RESERVED);
        assertThat(reservationMapper.selectById(created.getReservationId()).getStatus())
                .isEqualTo(ReservationStatusEnum.WAITING_CHECK_IN);

        reservationService.cancelReservation(created.getReservationId());

        assertThat(diningTableMapper.selectById(table.getId()).getStatus()).isEqualTo(TableStatusEnum.FREE);
        assertThat(reservationMapper.selectById(created.getReservationId()).getStatus())
                .isEqualTo(ReservationStatusEnum.CANCELED);
    }

    @Test
    void reservationRejectsOccupiedTableWithoutChangingExistingState() {
        User firstUser = insertUser();
        User secondUser = insertUser();
        DiningTable table = insertFreeTable();
        LoginContext.set(userLogin(firstUser.getId()));

        ReservationDTO request = reservationRequest(table.getId());
        ReservationCreateVO first = reservationService.createReservation(request);

        LoginContext.set(userLogin(secondUser.getId()));
        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("桌位已被占用")
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(BusinessErrorCode.RESERVATION_TABLE_OCCUPIED.getCode()));

        assertThat(reservationMapper.selectCount(new QueryWrapper<Reservation>()
                .eq("table_id", table.getId()))).isEqualTo(1);
        assertThat(reservationMapper.selectById(first.getReservationId()).getStatus())
                .isEqualTo(ReservationStatusEnum.WAITING_CHECK_IN);
        assertThat(diningTableMapper.selectById(table.getId()).getStatus())
                .isEqualTo(TableStatusEnum.RESERVED);
    }

    @Test
    void firstOrderMovesSessionAndTableToDiningAndPersistsSnapshot() {
        User user = insertUser();
        DiningTable table = insertFreeTable();
        Dish dish = insertOnSaleDish(1250);
        LoginContext.set(userLogin(user.getId()));
        DiningSessionVO session = diningSessionService.checkInTable(table.getId());

        DiningOrderCreateVO order = diningOrderService.createOrder(session.getSessionId(), orderRequest(dish.getId(), 2));

        assertThat(order.getTotalAmount()).isEqualTo(2500);
        assertThat(order.getStatus()).isEqualTo(OrderStatusEnum.PLACED.getCode());
        assertThat(diningSessionMapper.selectById(session.getSessionId()).getStatus())
                .isEqualTo(DiningSessionStatusEnum.DINING);
        assertThat(diningTableMapper.selectById(table.getId()).getStatus()).isEqualTo(TableStatusEnum.DINING);

        DiningOrder savedOrder = diningOrderMapper.selectById(order.getOrderId());
        assertThat(savedOrder.getTotalAmount()).isEqualTo(2500);
        assertThat(savedOrder.getSessionId()).isEqualTo(session.getSessionId());
        List<OrderItem> items = orderItemMapper.selectList(new QueryWrapper<OrderItem>()
                .eq("order_id", order.getOrderId()));
        assertThat(items).singleElement().satisfies(item -> {
            assertThat(item.getDishName()).isEqualTo(dish.getName());
            assertThat(item.getDishPrice()).isEqualTo(1250);
            assertThat(item.getQuantity()).isEqualTo(2);
            assertThat(item.getAmount()).isEqualTo(2500);
        });
    }

    @Test
    void orderStatusMustAdvanceOneStepAtATime() {
        DiningOrderCreateVO order = createOrderForUser();
        Employee employee = employeeLogin();
        LoginContext.set(employeeLoginInfo(employee.getId()));

        OrderStatusUpdateDTO request = new OrderStatusUpdateDTO();
        request.setStatus(OrderStatusEnum.SERVED.getCode());
        assertThatThrownBy(() -> diningOrderService.updateAdminOrderStatus(order.getOrderId(), request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("订单状态流转不合法")
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(BusinessErrorCode.INVALID_ORDER_STATE.getCode()));

        assertThat(diningOrderMapper.selectById(order.getOrderId()).getStatus())
                .isEqualTo(OrderStatusEnum.PLACED);
    }

    @Test
    void servedOrderCanCloseSessionAndReleaseTable() {
        DiningOrderCreateVO order = createOrderForUser();
        Employee employee = employeeLogin();
        LoginContext.set(employeeLoginInfo(employee.getId()));

        for (OrderStatusEnum status : List.of(OrderStatusEnum.COOKING, OrderStatusEnum.SERVED)) {
            OrderStatusUpdateDTO request = new OrderStatusUpdateDTO();
            request.setStatus(status.getCode());
            DiningOrderUpdateVO result = diningOrderService.updateAdminOrderStatus(order.getOrderId(), request);
            assertThat(result.getStatus()).isEqualTo(status.getCode());
        }

        DiningSession session = diningSessionMapper.selectById(order.getSessionId());
        DiningSessionCloseVO closed = diningSessionService.closeSession(session.getId());

        assertThat(closed.getSessionStatus()).isEqualTo(DiningSessionStatusEnum.COMPLETED.getCode());
        assertThat(closed.getTableStatus()).isEqualTo(TableStatusEnum.FREE.getCode());
        assertThat(diningOrderMapper.selectById(order.getOrderId()).getStatus())
                .isEqualTo(OrderStatusEnum.COMPLETED);
        DiningTable table = diningTableMapper.selectById(order.getTableId());
        assertThat(table.getStatus()).isEqualTo(TableStatusEnum.FREE);
        assertThat(table.getCurrentSessionId()).isNull();
        assertThat(diningSessionMapper.selectById(session.getId()).getActiveFlag()).isNull();
    }

    private DiningOrderCreateVO createOrderForUser() {
        User user = insertUser();
        DiningTable table = insertFreeTable();
        Dish dish = insertOnSaleDish(1000);
        LoginContext.set(userLogin(user.getId()));
        DiningSessionVO session = diningSessionService.checkInTable(table.getId());
        return diningOrderService.createOrder(session.getSessionId(), orderRequest(dish.getId(), 1));
    }

    private ReservationDTO reservationRequest(Long tableId) {
        ReservationDTO request = new ReservationDTO();
        request.setTableId(tableId);
        request.setPeopleCount(2);
        request.setReserveTime(LocalDateTime.now().plusHours(2));
        return request;
    }

    private OrderItemCreateDTO orderRequest(Long dishId, int quantity) {
        OrderItemDTO item = new OrderItemDTO();
        item.setDishId(dishId);
        item.setQuantity(quantity);
        OrderItemCreateDTO request = new OrderItemCreateDTO();
        request.setItems(List.of(item));
        return request;
    }

    private User insertUser() {
        int sequence = DATA_SEQUENCE.incrementAndGet();
        User user = User.builder()
                .phone("139" + String.format("%08d", sequence))
                .password("test-password")
                .nickname("状态机测试用户" + sequence)
                .status(UserStatusEnum.NORMAL)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        userMapper.insert(user);
        return user;
    }

    private DiningTable insertFreeTable() {
        int sequence = DATA_SEQUENCE.incrementAndGet();
        DiningTable table = DiningTable.builder()
                .tableNo("SM" + String.format("%04d", sequence))
                .capacity(4)
                .locationDesc("状态机测试桌位")
                .status(TableStatusEnum.FREE)
                .build();
        diningTableMapper.insert(table);
        return table;
    }

    private Dish insertOnSaleDish(int price) {
        int sequence = DATA_SEQUENCE.incrementAndGet();
        Dish dish = Dish.builder()
                .categoryId(1L)
                .name("状态机测试菜品" + sequence)
                .price(price)
                .status(com.foodflow.common.enums.DishStatusEnum.ON_SALE)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        dishMapper.insert(dish);
        return dish;
    }

    private Employee employeeLogin() {
        int sequence = DATA_SEQUENCE.incrementAndGet();
        Employee employee = Employee.builder()
                .phone("188" + String.format("%08d", sequence))
                .password("test-password")
                .name("状态机测试员工" + sequence)
                .role(EmployeeRoleEnum.STAFF)
                .status(com.foodflow.common.enums.EmployeeStatusEnum.NORMAL)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        employeeMapper.insert(employee);
        return employee;
    }

    private LoginInfo userLogin(Long userId) {
        return LoginInfo.builder().userId(userId).loginType(LoginTypeEnum.USER).build();
    }

    private LoginInfo employeeLoginInfo(Long employeeId) {
        return LoginInfo.builder()
                .employeeId(employeeId)
                .loginType(LoginTypeEnum.EMPLOYEE)
                .employeeRole(EmployeeRoleEnum.STAFF)
                .build();
    }
}
