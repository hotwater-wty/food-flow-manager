package com.foodflow.module.table.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodflow.module.diningsession.vo.DiningSessionVO;
import com.foodflow.module.table.service.DiningTableService;
import com.foodflow.module.table.vo.TableVO;
import com.foodflow.common.result.Result;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/user/tables")
@RequiredArgsConstructor
public class UserTableController {

    private final DiningTableService diningTableService;

    @GetMapping
    public Result<List<TableVO>> getDiningTables() {
        log.info("获取所有空闲桌位");
        List<TableVO> tableList = diningTableService.userFreeTableList();
        return Result.success(tableList);
    }

}
