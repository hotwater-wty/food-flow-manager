package com.foodflow.module.table.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodflow.common.result.Result;
import com.foodflow.module.table.dto.TableDTO;
import com.foodflow.module.table.service.DiningTableService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import lombok.extern.slf4j.Slf4j;
import java.util.List;
import com.foodflow.module.table.vo.TableVO;

@Slf4j
@RestController
@RequestMapping("/api/admin/tables")
@RequiredArgsConstructor
public class AdminTableController {

    private final DiningTableService diningTableService;

    /**
     * 创建桌位
     * 
     * @param tableDTO 桌位创建DTO
     * @return
     */
    @PostMapping
    public Result<Void> createTable(@Validated @RequestBody TableDTO tableDTO) {
        log.info("创建桌位: {}", tableDTO);
        diningTableService.createTable(tableDTO);
        return Result.success();
    }

    /**
     * 获取桌位列表
     * 
     * @return
     */
    @GetMapping
    public Result<List<TableVO>> getTableList() {
        log.info("获取桌位列表");
        List<TableVO> tableList = diningTableService.adminTableList();
        return Result.success(tableList);
    }

    /**
     * 获取桌位详情
     * 
     * @param tableId 桌位ID
     * @return
     */
    @GetMapping("/{tableId}")
    public Result<TableVO> getTableById(@PathVariable Long tableId) {
        log.info("获取桌位详情: {}", tableId);
        TableVO tableVO = diningTableService.getTableById(tableId);
        return Result.success(tableVO);
    }

    /**
     * 更新桌位
     * 
     * @param tableId  桌位ID
     * @param tableDTO 桌位更新DTO
     * @return
     */
    @PutMapping("/{tableId}")
    public Result<Void> updateTable(@PathVariable Long tableId,
            @Validated @RequestBody TableDTO tableDTO) {
        log.info("更新桌位: {}", tableId);
        diningTableService.updateTable(tableId, tableDTO);
        return Result.success();
    }

    /**
     * 删除桌位
     * 
     * @param tableId 桌位ID
     * @return
     */
    @DeleteMapping("/{tableId}")
    public Result<Void> deleteTable(@PathVariable Long tableId) {
        log.info("删除桌位: {}", tableId);
        diningTableService.deleteTable(tableId);
        return Result.success();
    }

    /**
     * 启用桌位
     * 
     * @param tableId     桌位ID
     * @return
     */
    @PostMapping("/{tableId}/enable")
    public Result<Void> enableTable(@PathVariable Long tableId) {
        log.info("启用桌位: {}", tableId);
        diningTableService.enableTable(tableId);
        return Result.success();
    }

    /**
     * 禁用桌位
     * 
     * @param tableId     桌位ID
     * @return
     */
    @PostMapping("/{tableId}/disable")
    public Result<Void> disableTable(@PathVariable Long tableId) {
        log.info("禁用桌位: {}", tableId);
        diningTableService.disableTable(tableId);
        return Result.success();
    }
}
