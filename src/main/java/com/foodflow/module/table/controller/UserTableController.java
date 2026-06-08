package com.foodflow.module.table.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodflow.module.table.service.DiningTableService;
import com.foodflow.module.table.vo.TableVO;
import com.foodflow.common.result.Result;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/user/tables")
@RequiredArgsConstructor
@Tag(name = "用户端-桌位查询", description = "用户端可用桌位查询接口")
public class UserTableController {

    private final DiningTableService diningTableService;

    @GetMapping
    @Operation(summary = "查询空闲桌位列表", description = "用户端查询当前可预约或可开台的空闲桌位")
    public Result<List<TableVO>> getDiningTables() {
        log.info("获取所有空闲桌位");
        List<TableVO> tableList = diningTableService.userFreeTableList();
        return Result.success(tableList);
    }

}
