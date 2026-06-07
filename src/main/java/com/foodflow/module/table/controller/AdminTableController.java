package com.foodflow.module.table.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "管理端-桌位管理", description = "管理端桌位新增、查询、修改、删除与启禁用接口")
public class AdminTableController {

    private final DiningTableService diningTableService;

    /**
     * 创建桌位
     * 
     * @param tableDTO 桌位创建DTO
     * @return
     */
    @PostMapping
    @Operation(summary = "新增桌位", description = "管理端新增餐厅桌位")
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
    @Operation(summary = "查询桌位列表", description = "管理端查询全部桌位列表")
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
    @Operation(summary = "查询桌位详情", description = "根据桌位ID查询桌位详情")
    public Result<TableVO> getTableById(
            @Parameter(description = "桌位ID", example = "1") @PathVariable Long tableId) {
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
    @Operation(summary = "修改桌位", description = "根据桌位ID修改桌号、容量和位置描述")
    public Result<Void> updateTable(
            @Parameter(description = "桌位ID", example = "1") @PathVariable Long tableId,
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
    @Operation(summary = "删除桌位", description = "根据桌位ID删除桌位")
    public Result<Void> deleteTable(
            @Parameter(description = "桌位ID", example = "1") @PathVariable Long tableId) {
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
    @Operation(summary = "启用桌位", description = "启用指定桌位，使其可被用户预约或扫码开台")
    public Result<Void> enableTable(
            @Parameter(description = "桌位ID", example = "1") @PathVariable Long tableId) {
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
    @Operation(summary = "禁用桌位", description = "禁用指定桌位，使其不可被用户预约或扫码开台")
    public Result<Void> disableTable(
            @Parameter(description = "桌位ID", example = "1") @PathVariable Long tableId) {
        log.info("禁用桌位: {}", tableId);
        diningTableService.disableTable(tableId);
        return Result.success();
    }
}
