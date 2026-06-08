package com.foodflow.module.employee.controller;

import com.foodflow.common.result.Result;
import com.foodflow.module.employee.dto.EmployeeRegisterDTO;
import com.foodflow.module.employee.service.EmployeeService;
import com.foodflow.module.employee.vo.EmployeeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/employees")
@Tag(name = "管理端-员工管理", description = "管理端员工新增、查询和启禁用接口")
public class AdminEmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @Operation(summary = "新增员工", description = "管理端新增员工账号")
    public Result<EmployeeVO> createEmployee(@Valid @RequestBody EmployeeRegisterDTO employeeRegisterDTO) {
        log.info("新增员工: {}", employeeRegisterDTO);
        return Result.success(employeeService.createEmployee(employeeRegisterDTO));
    }

    @GetMapping
    @Operation(summary = "查询员工列表", description = "管理端查询全部员工账号")
    public Result<List<EmployeeVO>> getEmployeeList() {
        log.info("查看员工列表");
        return Result.success(employeeService.getEmployeeList());
    }

    @GetMapping("/{employeeId}")
    @Operation(summary = "查询员工详情", description = "根据员工ID查询员工账号详情")
    public Result<EmployeeVO> getEmployeeById(
            @Parameter(description = "员工ID", example = "1") @PathVariable Long employeeId) {
        log.info("查看员工详情: {}", employeeId);
        return Result.success(employeeService.getEmployeeById(employeeId));
    }

    @PostMapping("/{employeeId}/disable")
    @Operation(summary = "禁用员工", description = "禁用指定员工账号")
    public Result<Void> disableEmployee(
            @Parameter(description = "员工ID", example = "1") @PathVariable Long employeeId) {
        log.info("禁用员工: {}", employeeId);
        employeeService.disableEmployee(employeeId);
        return Result.success();
    }

    @PostMapping("/{employeeId}/enable")
    @Operation(summary = "启用员工", description = "启用指定员工账号")
    public Result<Void> enableEmployee(
            @Parameter(description = "员工ID", example = "1") @PathVariable Long employeeId) {
        log.info("启用员工: {}", employeeId);
        employeeService.enableEmployee(employeeId);
        return Result.success();
    }
}
