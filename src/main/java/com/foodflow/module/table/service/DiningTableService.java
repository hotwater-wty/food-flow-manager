package com.foodflow.module.table.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.foodflow.module.table.dto.TableDTO;
import com.foodflow.module.table.entity.DiningTable;
import com.foodflow.module.table.vo.TableVO;

public interface DiningTableService extends IService<DiningTable> {

    List<TableVO> adminTableList();

    void createTable(TableDTO tableDTO);

    TableVO getTableById(Long tableId);

    void updateTable(Long tableId, TableDTO tableDTO);

    void deleteTable(Long tableId);

    List<TableVO> userFreeTableList();

    void enableTable(Long tableId);

    void disableTable(Long tableId);
    
}
