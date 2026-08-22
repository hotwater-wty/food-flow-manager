package com.foodflow.module.reservation.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodflow.common.dto.PageQueryDTO;
import com.foodflow.common.context.LoginContext;
import com.foodflow.common.enums.ReservationStatusEnum;
import com.foodflow.common.enums.TableStatusEnum;
import com.foodflow.common.exception.BusinessException;
import com.foodflow.common.result.PageResult;
import com.foodflow.common.utils.NumberUtils;
import com.foodflow.module.reservation.dto.ReservationDTO;
import com.foodflow.module.reservation.entity.Reservation;
import com.foodflow.module.reservation.mapper.ReservationMapper;
import com.foodflow.module.reservation.service.ReservationService;
import com.foodflow.module.reservation.vo.ReservationCreateVO;
import com.foodflow.module.reservation.vo.ReservationVO;
import com.foodflow.module.table.entity.DiningTable;
import com.foodflow.module.table.service.DiningTableService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> implements ReservationService {
    
    private final DiningTableService diningTableService;
    
    /**
     * 创建预约
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReservationCreateVO createReservation(ReservationDTO reservationDTO) {        
        DiningTable table = diningTableService.getById(reservationDTO.getTableId());
        if (table == null) {
            throw new BusinessException("桌位不存在");
        }
        if (table.getStatus() == TableStatusEnum.DISABLED) {
            throw new BusinessException("桌位已被禁用");
        }
        if (table.getStatus() != TableStatusEnum.FREE) {
            throw new BusinessException("桌位已被占用");
        }
        if(reservationDTO.getPeopleCount() > table.getCapacity()){
            throw new BusinessException("预约人数超过桌位容量");
        }
        if(reservationDTO.getReserveTime().isBefore(LocalDateTime.now())){
            throw new BusinessException("预约时间不能早于当前时间");
        }
        // 新增设定，预约天数不能过长，暂设为3天
        if(reservationDTO.getReserveTime().isAfter(LocalDateTime.now().plusDays(3))){
            throw new BusinessException("预约天数不能超过3天");
        }
        // TODO 新增设定，用户设定预约时间后并非全程不允许其他用户使用，v2引入
        // 当前简单设计，预约时间内都不允许其他用户使用该桌位

        // TODO 后续需处理并发问题
        
        // 使用数据库条件更新，确保并发安全
        boolean updateResult = diningTableService.lambdaUpdate()
                        .eq(DiningTable::getId, reservationDTO.getTableId())
                        .eq(DiningTable::getStatus, TableStatusEnum.FREE)
                        .ge(DiningTable::getCapacity, reservationDTO.getPeopleCount())
                        .set(DiningTable::getStatus, TableStatusEnum.RESERVED)
                        .set(DiningTable::getUpdateTime, LocalDateTime.now())
                        .update();
        if (!updateResult) {
            throw new BusinessException("桌位已被占用");
        }
        // 创建预约，将桌位预约状态设置为待到店  
        Reservation reservation = toReservation(reservationDTO);
        // 保存预约数据到数据库
        save(reservation);
        // 封装对象返回数据
        return toCreateVO(reservation);
    }

    /**
     * 用户查看预约列表
     */
    @Override
    public List<ReservationVO> getReservation() {
        List<Reservation> reservationList = query()
                .eq("user_id", LoginContext.getUserId())
                .list();
        return reservationList.stream()
                .map(this::toVO)
                .toList();
    }

    /**
     * 获取所有预约列表
     */
    @Override
    public PageResult<ReservationVO> getAllReservation(PageQueryDTO pageQueryDTO) {
        Page<Reservation> pageParam = new Page<>(pageQueryDTO.getPageNo(), pageQueryDTO.getPageSize());
        Page<Reservation> reservationPage = page(pageParam, query()
                .orderByDesc("create_time")
                .getWrapper());
        List<ReservationVO> records = reservationPage.getRecords().stream()
                .map(this::toVO)
                .toList();
        return new PageResult<>(
                reservationPage.getTotal(),
                pageQueryDTO.getPageNo(),
                pageQueryDTO.getPageSize(),
                records);
    }

    /**
     * 用户取消预约
     */
    @Override
    @Transactional(rollbackFor = Exception.class)   
    public void cancelReservation(Long reservationId) {
        Reservation reservation = getById(reservationId);
        if (reservation == null) {
            throw new BusinessException("预约不存在");
        }
        if (reservation.getStatus() != ReservationStatusEnum.WAITING_CHECK_IN) {
            throw new BusinessException("预约状态错误，请重试");
        }
        if (!reservation.getUserId().equals(LoginContext.getUserId())) {
            throw new BusinessException("只能操作自己的预约");
        }
        // 更新预约状态为已取消
        reservation.setStatus(ReservationStatusEnum.CANCELED);
        reservation.setCancelTime(LocalDateTime.now());
        reservation.setUpdateTime(LocalDateTime.now());
        updateById(reservation);
        // 更新桌位状态为空闲
        boolean updateResult = diningTableService.lambdaUpdate()
                .eq(DiningTable::getId, reservation.getTableId())
                .eq(DiningTable::getStatus, TableStatusEnum.RESERVED)
                .set(DiningTable::getStatus, TableStatusEnum.FREE)
                .set(DiningTable::getUpdateTime, LocalDateTime.now())
                .update();
        if (!updateResult) {
            throw new BusinessException("桌位状态更新失败，请重试");
        }
    }

    /**
     * 商户端取消异常预约
     */
    @Override
    @Transactional(rollbackFor = Exception.class)   
    public void cancelAdminReservation(Long reservationId) {
        Reservation reservation = getById(reservationId);
        if (reservation == null) {
            throw new BusinessException("预约不存在");
        }
        if (reservation.getStatus() != ReservationStatusEnum.WAITING_CHECK_IN) {
            throw new BusinessException("预约状态错误，请重试");
        }

        LocalDateTime now = LocalDateTime.now();

        // 更新预约状态为已取消
        boolean reservationUpdated = lambdaUpdate()
                        .eq(Reservation::getId, reservationId)
                        .eq(Reservation::getStatus, ReservationStatusEnum.WAITING_CHECK_IN)
                        .set(Reservation::getStatus, ReservationStatusEnum.CANCELED)
                        .set(Reservation::getCancelTime, now)
                        .set(Reservation::getUpdateTime, now)
                        .update();
        if (!reservationUpdated) {
            throw new BusinessException("预约状态更新失败，请重试");
        }

        // 更新桌位状态为空闲
        boolean tableUpdated = diningTableService.lambdaUpdate()
                .eq(DiningTable::getId, reservation.getTableId())
                .eq(DiningTable::getStatus, TableStatusEnum.RESERVED)
                .set(DiningTable::getStatus, TableStatusEnum.FREE)
                .set(DiningTable::getUpdateTime, LocalDateTime.now())
                .update();
        if (!tableUpdated) {
            throw new BusinessException("桌位状态更新失败，请重试");
        }
    }
    
    /**
     * 转换预约DTO为预约实体
     */
    private Reservation toReservation(ReservationDTO reservationDTO) {
        return Reservation.builder()
                .reservationNo(NumberUtils.generateReservationNo())
                .userId(LoginContext.getUserId())
                .tableId(reservationDTO.getTableId())
                .peopleCount(reservationDTO.getPeopleCount())
                .reserveTime(reservationDTO.getReserveTime())
                .status(ReservationStatusEnum.WAITING_CHECK_IN)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }

    private ReservationCreateVO toCreateVO(Reservation reservation) {
        return ReservationCreateVO.builder()
                .reservationId(reservation.getId())
                .reservationNo(reservation.getReservationNo())
                .tableId(reservation.getTableId())
                .peopleCount(reservation.getPeopleCount())
                .reserveTime(reservation.getReserveTime())
                .status(reservation.getStatus().getCode())
                .build();
    }

    private ReservationVO toVO(Reservation reservation) {
        return ReservationVO.builder()
                .reservationId(reservation.getId())
                .reservationNo(reservation.getReservationNo())
                .tableId(reservation.getTableId())
                .peopleCount(reservation.getPeopleCount())
                .reserveTime(reservation.getReserveTime())
                .status(reservation.getStatus().getCode())
                .build();
    }

    /**
     * 获取用户预约详情
     */
    @Override
    public ReservationVO getUserReservationDetail(Long reservationId) {
        Reservation reservation = getById(reservationId);
        if (reservation == null) {
            throw new BusinessException("预约不存在");
        }
        if (!reservation.getUserId().equals(LoginContext.getUserId())) {
            throw new BusinessException("只能查看自己的预约");
        }
        return toVO(reservation);
    }

    @Override
    public ReservationVO getAdminReservationDetail(Long reservationId) {
        Reservation reservation = getById(reservationId);
        if (reservation == null) {
            throw new BusinessException("预约不存在");
        }
        return toVO(reservation);
    }


   
}
