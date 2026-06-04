package com.foodflow.module.diningsession.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodflow.common.context.LoginContext;
import com.foodflow.common.enums.DiningSessionStatusEnum;
import com.foodflow.common.enums.TableStatusEnum;
import com.foodflow.common.exception.BusinessException;
import com.foodflow.module.diningsession.entity.DiningSession;
import com.foodflow.module.diningsession.mapper.DiningSessionMapper;
import com.foodflow.module.diningsession.service.DiningSessionService;
import com.foodflow.module.diningsession.vo.DiningSessionVO;
import com.foodflow.module.table.entity.DiningTable;
import com.foodflow.module.table.service.DiningTableService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class DiningSessionServiceImpl extends ServiceImpl<DiningSessionMapper, DiningSession>
        implements DiningSessionService {

    private final DiningTableService diningTableService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelAdminSession(Long sessionId) {
        DiningSession diningSession = getById(sessionId);
        if (diningSession == null) {
            throw new BusinessException("用餐会话不存在");
        }
        if (diningSession.getStatus() == DiningSessionStatusEnum.COMPLETED || 
            diningSession.getStatus() == DiningSessionStatusEnum.CANCELED) {
            throw new BusinessException("用餐会话已完成或已取消");
        }
        // 分别处理等待中和用餐中的状态
        // 关闭会话并释放桌位
        diningSession.setStatus(DiningSessionStatusEnum.CANCELED);
        // 释放桌位
        DiningTable table = diningTableService.getById(diningSession.getTableId());
        // 已从桌位模块中确保业务状态的桌位不能更改、禁用或删除
        // 预约状态 已到店为终态之一，无需处理
        // TODO 为保证健壮性，这里可能需要做异常处理，v2开始考虑
        table.setStatus(TableStatusEnum.FREE);
        table.setCurrentSessionId(null);
        updateById(diningSession);
        diningTableService.updateById(table);
    }

    @Override
    public DiningSessionVO getCurrentSession() {
        DiningSession diningSession = query()
                .eq("userId", LoginContext.getUserId())
                .one();
        if (diningSession == null) {
            throw new BusinessException("当前用户没有用餐会话");
        }
        DiningTable table = diningTableService.getById(diningSession.getTableId());
        if (table == null) {
            throw new BusinessException("用餐会话关联的餐桌不存在");
        }

        return DiningSessionVO.builder()
                .sessionId(diningSession.getId())
                .sessionOn(diningSession.getSessionOn())
                .reservationId(diningSession.getReservationId())
                .tableId(diningSession.getTableId())
                .tableOn(table.getTableNo())
                .sessionStatus(diningSession.getStatus().getCode())
                .tableStatus(table.getStatus().getCode())
                .build();
    }

}
