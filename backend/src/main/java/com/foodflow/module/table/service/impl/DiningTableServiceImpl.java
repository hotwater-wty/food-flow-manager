package com.foodflow.module.table.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodflow.common.dto.PageQueryDTO;
import com.foodflow.common.enums.TableStatusEnum;
import com.foodflow.common.exception.BusinessException;
import com.foodflow.common.result.PageResult;
import com.foodflow.module.table.dto.TableDTO;
import com.foodflow.module.table.entity.DiningTable;
import com.foodflow.module.table.mapper.DiningTableMapper;
import com.foodflow.module.table.service.DiningTableService;
import com.foodflow.module.table.vo.TableVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiningTableServiceImpl extends ServiceImpl<DiningTableMapper, DiningTable> implements DiningTableService {

    /**
     * 商户端查询桌位列表(全部信息)
     */
    @Override
    public PageResult<TableVO> adminTableList(PageQueryDTO pageQueryDTO) {
        Page<DiningTable> pageParam = new Page<>(pageQueryDTO.getPageNo(), pageQueryDTO.getPageSize());
        Page<DiningTable> tablePage = page(pageParam, query()
                .orderByAsc("id")
                .getWrapper());
        List<TableVO> records = tablePage.getRecords().stream()
                .map(this::toTableVO)
                .collect(Collectors.toList());
        return new PageResult<>(
                tablePage.getTotal(),
                pageQueryDTO.getPageNo(),
                pageQueryDTO.getPageSize(),
                records);
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
        // BeanUtils.copyProperties(tableDTO, diningTable);
        diningTable.setTableNo(tableDTO.getTableNo());
        diningTable.setCapacity(tableDTO.getCapacity());
        diningTable.setLocationDesc(tableDTO.getLocationDesc());
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
            throw new BusinessException("不能重复启用");
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
        if (diningTable.getStatus() == TableStatusEnum.DISABLED) {
            throw new BusinessException("不能重复禁用");
        }
        if (diningTable.getStatus() != TableStatusEnum.FREE) {
            throw new BusinessException("当前桌位处于业务状态，不能禁用");
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

}
