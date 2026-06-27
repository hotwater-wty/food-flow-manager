package com.foodflow.module.diningsession.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodflow.common.context.LoginContext;
import com.foodflow.common.enums.ActiveFlagEnum;
import com.foodflow.common.enums.DiningSessionStatusEnum;
import com.foodflow.common.enums.OrderStatusEnum;
import com.foodflow.common.enums.ReservationStatusEnum;
import com.foodflow.common.enums.TableStatusEnum;
import com.foodflow.common.exception.BusinessException;
import com.foodflow.common.utils.NumberUtils;
import com.foodflow.module.diningorder.entity.DiningOrder;
import com.foodflow.module.diningorder.mapper.DiningOrderMapper;
import com.foodflow.module.diningsession.dto.DiningSessionDTO;
import com.foodflow.module.diningsession.entity.DiningSession;
import com.foodflow.module.diningsession.mapper.DiningSessionMapper;
import com.foodflow.module.diningsession.service.DiningSessionService;
import com.foodflow.module.diningsession.vo.DiningSessionCloseVO;
import com.foodflow.module.diningsession.vo.DiningSessionVO;
import com.foodflow.module.reservation.entity.Reservation;
import com.foodflow.module.reservation.service.ReservationService;
import com.foodflow.module.table.entity.DiningTable;
import com.foodflow.module.table.service.DiningTableService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class DiningSessionServiceImpl extends ServiceImpl<DiningSessionMapper, DiningSession>
        implements DiningSessionService {

    private final DiningTableService diningTableService;
    private final ReservationService reservationService;
    private final DiningOrderMapper diningOrderMapper;

    /**
     * 取消等待中的用餐会话
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DiningSessionCloseVO cancelWaitingSession(Long sessionId) {
        DiningSession diningSession = getById(sessionId);
        if (diningSession == null) {
            throw new BusinessException("用餐会话不存在");
        }
        if (diningSession.getStatus() == DiningSessionStatusEnum.COMPLETED || 
            diningSession.getStatus() == DiningSessionStatusEnum.CANCELED) {
            throw new BusinessException("用餐会话已完成或已取消");
        }
        if(diningSession.getStatus() == DiningSessionStatusEnum.DINING) {
            throw new BusinessException("不能取消用餐中的会话");
        }
        // 关闭会话并释放桌位
        diningSession.setStatus(DiningSessionStatusEnum.CANCELED);
        // 释放桌位
        DiningTable table = diningTableService.getById(diningSession.getTableId());
        // 已从桌位模块中确保业务状态的桌位不能更改、禁用或删除
        // 此处保证Service层健壮性再审查一遍
        if (table == null) {
            throw new BusinessException("用餐会话关联的餐桌不存在");
        }
        if (table.getStatus() != TableStatusEnum.WAITING) {
            throw new BusinessException("用餐会话关联的餐桌状态异常");
        }
        // 预约状态 已到店为终态之一，无需处理
        // TODO 为保证健壮性，这里可能需要做异常处理，v2开始考虑
        table.setStatus(TableStatusEnum.FREE);
        table.setCurrentSessionId(null);
        updateById(diningSession);
        diningTableService.updateById(table);
        return toDiningSessionCloseVO(diningSession, table);
    }

    /**
     * 获取当前用户等待中的或用餐中的会话
     */
    @Override
    public DiningSessionVO getCurrentSession() {
        // 过滤掉已完成和已取消的会话
        // 只返回当前用户的等待中或用餐中的会话
        DiningSession diningSession = query()
                .eq("user_id", LoginContext.getUserId())
                .in("status", DiningSessionStatusEnum.WAITING,
                     DiningSessionStatusEnum.DINING)
                .one();
        if (diningSession == null) {
            throw new BusinessException("当前用户没有用餐会话");
        }
        DiningTable table = diningTableService.getById(diningSession.getTableId());
        if (table == null) {
            throw new BusinessException("用餐会话关联的餐桌不存在");
        }

        return toDiningSessionVO(diningSession, table);
    }

    /**
     * 预约用户扫码到店
     * @param reservationId 预约ID
     * @param tableId 桌位ID
     * @return 会话VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DiningSessionVO checkInReservation(Long reservationId, Long tableId) {
        // 审查预约状态
        Reservation reservation = reservationService.getById(reservationId);
        if (reservation == null) {
            throw new BusinessException("预约不存在");
        }
        if (reservation.getStatus() != ReservationStatusEnum.WAITING_CHECK_IN) {
            throw new BusinessException("预约状态错误，请重试");
        }
        if (!reservation.getUserId().equals(LoginContext.getUserId())) {
            throw new BusinessException("只能操作自己的预约");
        }
        // 审查桌位状态
        DiningTable currentTable = diningTableService.getById(tableId);
        if (currentTable == null) {
            throw new BusinessException("桌位不存在");
        }
        if (currentTable.getStatus() != TableStatusEnum.RESERVED) {
            throw new BusinessException("桌位状态错误");
        }
        // 扫码桌位必须与预约桌位一致
        if (!reservation.getTableId().equals(currentTable.getId())) {
            throw new BusinessException("扫码桌位与预约桌位不一致");
        }
        // 用户当前是否有用餐会话(用作友好提示，业务层面无法真的避免并发问题，由数据库层面约束)
        DiningSession currentSession = lambdaQuery()
                .eq(DiningSession::getUserId, LoginContext.getUserId())
                .eq(DiningSession::getActiveFlag, ActiveFlagEnum.ACTIVE)
                .one();
        if (currentSession != null) {
            throw new BusinessException("当前用户已存在用餐会话");
        }

        LocalDateTime now = LocalDateTime.now();

        // 更新预约状态为已到店
        boolean reservationUpdated = reservationService.lambdaUpdate()
                        .eq(Reservation::getId, reservationId)
                        .eq(Reservation::getUserId, LoginContext.getUserId())
                        .eq(Reservation::getTableId, tableId)
                        .eq(Reservation::getStatus, ReservationStatusEnum.WAITING_CHECK_IN)
                        .set(Reservation::getStatus, ReservationStatusEnum.CHECKED_IN)
                        .set(Reservation::getCheckInTime, now)
                        .set(Reservation::getUpdateTime, now)
                        .update();
        if (!reservationUpdated) {
            throw new BusinessException("预约状态更新失败，请重试");
        }
        
        // 构建会话
        DiningSession diningSession = getDiningSession(reservation);
        try{
            save(diningSession);    // 数据库active_flag字段唯一约束，重复则抛出异常
        } catch (DuplicateKeyException e) {
            throw new BusinessException("当前用户已存在用餐会话，请勿重复开台");
        }

        // 更新桌位状态
        boolean tableUpdated = diningTableService.lambdaUpdate()
                        .eq(DiningTable::getId, tableId)
                        .eq(DiningTable::getStatus, TableStatusEnum.RESERVED)
                        .set(DiningTable::getStatus, TableStatusEnum.WAITING)
                        .set(DiningTable::getCurrentSessionId, diningSession.getId())
                        .set(DiningTable::getUpdateTime, now)
                        .update();
        if (!tableUpdated) {
            throw new BusinessException("桌位状态更新失败，请重试");
        }
        
        // 构建会话VO
        return toDiningSessionVO(diningSession, currentTable);
    }

    /**
     * 非预约用户扫码占座
     * @param tableId 桌位ID
     * @return 会话VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DiningSessionVO checkInTable(Long tableId) {
        DiningTable diningTable = diningTableService.getById(tableId);
        if (diningTable == null || diningTable.getStatus() == TableStatusEnum.DISABLED) {
            throw new BusinessException("桌位不存在或已禁用");
        }
        if (diningTable.getStatus() != TableStatusEnum.FREE) {
            throw new BusinessException("该桌位已被使用");
        }

        // TODO 需处理并发问题

        // 构建会话
        DiningSession diningSession = getDiningSession(tableId);
        saveOrUpdate(diningSession);

        // 更新桌位状态
        diningTable.setStatus(TableStatusEnum.WAITING);
        diningTable.setCurrentSessionId(diningSession.getId());
        diningTableService.updateById(diningTable);
        
        // 构建会话VO
        return toDiningSessionVO(diningSession, diningTable);
    }

    private DiningSessionVO toDiningSessionVO(
                DiningSession diningSession, DiningTable diningTable) {
        return DiningSessionVO.builder()
                .sessionId(diningSession.getId())
                .sessionNo(diningSession.getSessionNo())
                .tableId(diningSession.getTableId())
                .tableNo(diningTable.getTableNo())
                .sessionStatus(diningSession.getStatus().getCode())
                .tableStatus(diningTable.getStatus().getCode())
                .build();
    }

    private DiningSession getDiningSession(Long tableId) {
        return DiningSession.builder()
                .sessionNo(NumberUtils.generateSessionNo())
                .userId(LoginContext.getUserId())
                .tableId(tableId)
                .reservationId(null)
                .status(DiningSessionStatusEnum.WAITING)
                .activeFlag(ActiveFlagEnum.ACTIVE)
                .openTime(LocalDateTime.now())
                .firstOrderTime(null)
                .closeTime(null)
                .closeEmployeeId(null)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }

    private DiningSession getDiningSession(Reservation reservation) {
        return DiningSession.builder()
                .sessionNo(NumberUtils.generateSessionNo())
                .userId(LoginContext.getUserId())
                .tableId(reservation.getTableId())
                .reservationId(reservation.getId())
                .status(DiningSessionStatusEnum.WAITING)
                .activeFlag(ActiveFlagEnum.ACTIVE)
                .openTime(LocalDateTime.now())
                .firstOrderTime(null)
                .closeTime(null)
                .closeEmployeeId(null)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }

    @Override
    public List<DiningSessionVO> getSessionList(DiningSessionDTO diningSessionDTO) {
        DiningSessionStatusEnum status = diningSessionDTO.getStatusEnum();
        List<DiningSession> sessionList = querySessionList(diningSessionDTO, status);
        Map<Long, DiningTable> tableMap = getTableMap(sessionList);
        return sessionList.stream()
                .map(session -> toDiningSessionVO(session, tableMap))
                .collect(Collectors.toList());
    }

    private List<DiningSession> querySessionList(
                DiningSessionDTO diningSessionDTO, DiningSessionStatusEnum status) {
        return query()
                .eq(diningSessionDTO.getReservationId() != null,
                        "reservation_id", diningSessionDTO.getReservationId())
                .eq(diningSessionDTO.getTableId() != null,
                        "table_id", diningSessionDTO.getTableId())
                .eq(diningSessionDTO.getSessionId() != null,
                        "id", diningSessionDTO.getSessionId())
                .eq(status != null, "status", status)
                .list();
    }

    private Map<Long, DiningTable> getTableMap(List<DiningSession> sessionList) {
        if (sessionList.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> tableIds = sessionList.stream()
                .map(DiningSession::getTableId)
                .distinct()
                .collect(Collectors.toList());
        return diningTableService.listByIds(tableIds)
                .stream()
                .collect(Collectors.toMap(DiningTable::getId, Function.identity()));
    }

    private DiningSessionVO toDiningSessionVO(
                DiningSession session, Map<Long, DiningTable> tableMap) {
        DiningTable table = tableMap.get(session.getTableId());
        if (table == null) {
            throw new BusinessException("用餐会话关联的餐桌不存在");
        }
        return DiningSessionVO.builder()
                .sessionId(session.getId())
                .sessionNo(session.getSessionNo())
                .tableId(session.getTableId())
                .tableNo(table.getTableNo())
                .sessionStatus(session.getStatus().getCode())
                .tableStatus(table.getStatus().getCode())
                .build();
    }

    @Override
    public DiningSessionVO getSessionDetail(Long sessionId) {
        DiningSession session = getById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        DiningTable table = diningTableService.getById(session.getTableId());
        if (table == null) {
            throw new BusinessException("桌位不存在");
        }
        return toDiningSessionVO(session, table);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DiningSessionCloseVO closeSession(Long sessionId) {
        // TODO 要加锁，确保并发安全
        DiningSession session = getById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        if (session.getStatus() != DiningSessionStatusEnum.DINING) {
            throw new BusinessException("会话状态错误");
        }

        DiningTable table = diningTableService.getById(session.getTableId());
        if (table == null) {
            throw new BusinessException("桌位不存在");
        }
        if (table.getStatus() != TableStatusEnum.DINING) {
            throw new BusinessException("桌位状态错误");
        }
        if (!Objects.equals(table.getCurrentSessionId(), sessionId)) {
            throw new BusinessException("桌位与会话匹配异常");
        }
        
        List<DiningOrder> orderList = diningOrderMapper.selectList(
                new LambdaQueryWrapper<DiningOrder>()
                        .eq(DiningOrder::getSessionId, sessionId));
        if (orderList.isEmpty()) {
            throw new BusinessException("订单不存在");
        }

        // 订单状态更新需要校验
        LocalDateTime now = LocalDateTime.now();
        for (DiningOrder order : orderList) {
            if (order.getStatus() == OrderStatusEnum.PLACED
             || order.getStatus() == OrderStatusEnum.COOKING) {
                throw new BusinessException("订单未完成，不能关闭会话");
            }
            if (order.getStatus() == OrderStatusEnum.COMPLETED
             || order.getStatus() == OrderStatusEnum.CANCELED) {
                continue;
            }
        }
        DiningOrder orderUpdate = DiningOrder.builder()
                .status(OrderStatusEnum.COMPLETED)
                .updateTime(now)
                .build();
        diningOrderMapper.update(orderUpdate,
                new LambdaUpdateWrapper<DiningOrder>()
                        .eq(DiningOrder::getSessionId, sessionId)
                        .eq(DiningOrder::getStatus, OrderStatusEnum.SERVED));
        
        session.setStatus(DiningSessionStatusEnum.COMPLETED);
        session.setUpdateTime(now);
        session.setCloseTime(now);
        session.setCloseEmployeeId(LoginContext.getEmployeeId());
        updateById(session);
        
        // 桌位状态更新要往后放，减轻并发风险
        table.setStatus(TableStatusEnum.FREE);
        table.setUpdateTime(now);
        table.setCurrentSessionId(null);
        diningTableService.updateById(table);   

        return toDiningSessionCloseVO(session, table);
    }

    private DiningSessionCloseVO toDiningSessionCloseVO(DiningSession session, DiningTable table) {
        return DiningSessionCloseVO.builder()
                .sessionId(session.getId())
                .sessionNo(session.getSessionNo())
                .tableId(session.getTableId())
                .tableNo(table.getTableNo())
                .sessionStatus(session.getStatus().getCode())
                .tableStatus(table.getStatus().getCode())
                .closeTime(session.getCloseTime())
                .closeEmployeeId(session.getCloseEmployeeId())
                .build();
    }
}
