package com.foodflow.module.employee.controller;

import com.foodflow.common.result.Result;
import com.foodflow.module.employee.dto.EmployeeRegisterDTO;
import com.foodflow.module.employee.service.EmployeeService;
import com.foodflow.module.employee.vo.EmployeeVO;
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
public class AdminEmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public Result<EmployeeVO> createEmployee(@Valid @RequestBody EmployeeRegisterDTO employeeRegisterDTO) {
        log.info("新增员工: {}", employeeRegisterDTO);
        return Result.success(employeeService.createEmployee(employeeRegisterDTO));
    }

    @GetMapping
    public Result<List<EmployeeVO>> getEmployeeList() {
        log.info("查看员工列表");
        return Result.success(employeeService.getEmployeeList());
    }

    @GetMapping("/{employeeId}")
    public Result<EmployeeVO> getEmployeeById(@PathVariable Long employeeId) {
        log.info("查看员工详情: {}", employeeId);
        return Result.success(employeeService.getEmployeeById(employeeId));
    }

    @PostMapping("/{employeeId}/disable")
    public Result<Void> disableEmployee(@PathVariable Long employeeId) {
        log.info("禁用员工: {}", employeeId);
        employeeService.disableEmployee(employeeId);
        return Result.success();
    }

    @PostMapping("/{employeeId}/enable")
    public Result<Void> enableEmployee(@PathVariable Long employeeId) {
        log.info("启用员工: {}", employeeId);
        employeeService.enableEmployee(employeeId);
        return Result.success();
    }
}
