package com.foodflow.module.table.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodflow.common.context.LoginContext;
import com.foodflow.common.enums.DiningSessionStatusEnum;
import com.foodflow.common.enums.TableStatusEnum;
import com.foodflow.common.exception.BusinessException;
import com.foodflow.common.utils.NumberUtils;
import com.foodflow.module.diningsession.entity.DiningSession;
import com.foodflow.module.diningsession.service.DiningSessionService;
import com.foodflow.module.diningsession.vo.DiningSessionVO;
import com.foodflow.module.table.dto.TableDTO;
import com.foodflow.module.table.entity.DiningTable;
import com.foodflow.module.table.mapper.DiningTableMapper;
import com.foodflow.module.table.service.DiningTableService;
import com.foodflow.module.table.vo.TableVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiningTableServiceImpl extends ServiceImpl<DiningTableMapper, DiningTable> implements DiningTableService {

    private final DiningSessionService diningSessionService;
    // TODO 后续要做分页查询

    /**
     * 商户端查询桌位列表(全部信息)
     */
    @Override
    public List<TableVO> adminTableList() {
        List<DiningTable> list = query().list();
        List<TableVO> tableVOList = list.stream()
                .map(this::toTableVO)
                .collect(Collectors.toList());
        return tableVOList;
    }

    @Override
    public void createTable(TableDTO tableDTO) {
        DiningTable diningTable = DiningTable.builder()
                .tableNo(tableDTO.getTableNo())
                .capacity(tableDTO.getCapacity())
                .locationDesc(tableDTO.getLocationDesc())
                .status(TableStatusEnum.FREE)
                .currentSessionId(null)
                .build();
        saveOrUpdate(diningTable);
    }

    @Override
    public TableVO getTableById(Long tableId) {
        DiningTable diningTable = getById(tableId);
        if (diningTable == null) {
            throw new BusinessException("桌位不存在");
        }
        return toTableVO(diningTable);
    }

    /**
     * 更新桌位资料
     */
    @Override
    public void updateTable(Long tableId, TableDTO tableDTO) {
        DiningTable diningTable = getById(tableId);
        if (diningTable == null) {
            throw new BusinessException("桌位不存在");
        }
        if (diningTable.getStatus() != TableStatusEnum.DISABLED
                && diningTable.getStatus() != TableStatusEnum.FREE) {
            throw new BusinessException("业务状态的桌位不能更新");
        }
        BeanUtils.copyProperties(tableDTO, diningTable);
        diningTable.setId(tableId);
        updateById(diningTable);
    }

    /**
     * 删除桌位
     * 
     * @param tableId 桌位ID
     */
    @Override
    public void deleteTable(Long tableId) {
        // TODO 需设定为店长权限
        DiningTable diningTable = getById(tableId);
        if (diningTable == null) {
            throw new BusinessException("桌位不存在");
        }
        if (diningTable.getStatus() != TableStatusEnum.DISABLED
                && diningTable.getStatus() != TableStatusEnum.FREE) {
            throw new BusinessException("业务状态的桌位不能删除");
        }
        removeById(tableId);
    }

    /**
     * 用户端查询空闲桌位
     */
    @Override
    public List<TableVO> userFreeTableList() {
        List<DiningTable> list = query().eq("status", TableStatusEnum.FREE).list();
        List<TableVO> tableVOList = list.stream()
                .map(this::toTableVO)
                .collect(Collectors.toList());
        return tableVOList;
    }

    /**
     * 启用桌位
     */
    @Override
    public void enableTable(Long tableId) {
        
        DiningTable diningTable = getById(tableId);
        if (diningTable == null) {
            throw new BusinessException("桌位不存在");
        }
        if (diningTable.getStatus() != TableStatusEnum.DISABLED) {
            throw new BusinessException("非禁用状态的桌位不能启用");
        }
        diningTable.setStatus(TableStatusEnum.FREE);
        updateById(diningTable);
    }

    /**
     * 禁用桌位
     */
    @Override
    public void disableTable(Long tableId) {
        DiningTable diningTable = getById(tableId);
        if (diningTable == null) {
            throw new BusinessException("桌位不存在");
        }
        if (diningTable.getStatus() != TableStatusEnum.FREE) {
            throw new BusinessException("非空闲状态的桌位不能禁用");
        }
        diningTable.setStatus(TableStatusEnum.DISABLED);
        updateById(diningTable);
    }

    private TableVO toTableVO(DiningTable diningTable) {
        return TableVO.builder()
                .tableId(diningTable.getId())
                .tableNo(diningTable.getTableNo())
                .capacity(diningTable.getCapacity())
                .locationDesc(diningTable.getLocationDesc())
                .status(diningTable.getStatus().getCode())
                .currentSessionId(diningTable.getCurrentSessionId())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DiningSessionVO checkInTable(Long tableId) {
        DiningTable diningTable = getById(tableId);
        if (diningTable == null || diningTable.getStatus() == TableStatusEnum.DISABLED) {
            throw new BusinessException("桌位不存在或已禁用");
        }
        if (diningTable.getStatus() != TableStatusEnum.FREE) {
            throw new BusinessException("该桌位已被使用");
        }

        // TODO 需处理并发问题

        // 构建会话
        DiningSession diningSession = getDiningSession(tableId);
        diningSessionService.saveOrUpdate(diningSession);

        // 更新桌位状态
        diningTable.setStatus(TableStatusEnum.WAITING);
        diningTable.setCurrentSessionId(diningSession.getId());
        updateById(diningTable);
        
        // 构建会话VO
        DiningSessionVO diningSessionVO = DiningSessionVO.builder()
                .sessionId(diningSession.getId())
                .sessionOn(diningSession.getSessionOn())
                .reservationId(diningSession.getReservationId())
                .tableId(diningSession.getTableId())
                .tableOn(diningTable.getTableNo())
                .sessionStatus(diningSession.getStatus().getCode())
                .tableStatus(diningTable.getStatus().getCode())
                .build();
        return diningSessionVO;
    }

    private DiningSession getDiningSession(Long tableId) {
        return DiningSession.builder()
                .sessionOn(NumberUtils.generateSessionOn())
                .userId(LoginContext.getUserId())
                .tableId(tableId)
                .reservationId(null)
                .status(DiningSessionStatusEnum.WAITING)
                .openTime(LocalDateTime.now())
                .firstOrderTime(null)
                .closeTime(null)
                .closeEmployeeId(null)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }
}
