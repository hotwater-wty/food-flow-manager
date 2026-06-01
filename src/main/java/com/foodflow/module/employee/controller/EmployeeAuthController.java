package com.foodflow.module.employee.controller;

import com.foodflow.common.result.Result;
import com.foodflow.module.employee.dto.EmployeeLoginDTO;
import com.foodflow.module.employee.dto.EmployeeRegisterDTO;
import com.foodflow.module.employee.service.EmployeeService;
import com.foodflow.module.employee.vo.EmployeeLoginVO;
import com.foodflow.module.employee.vo.EmployeeRegisterVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/auth")
public class EmployeeAuthController {

    private final EmployeeService employeeService;

    @PostMapping("/register")
    public Result<EmployeeRegisterVO> register(@Valid @RequestBody EmployeeRegisterDTO employeeRegisterDTO) {
        log.info("员工注册: {}", employeeRegisterDTO);
        EmployeeRegisterVO employeeRegisterVO  = employeeService.register(employeeRegisterDTO);
        return Result.success(employeeRegisterVO);
    }

    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@Valid @RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录: {}", employeeLoginDTO);
        EmployeeLoginVO employeeLoginVO = employeeService.login(employeeLoginDTO);
        return Result.success(employeeLoginVO);
    }
}
