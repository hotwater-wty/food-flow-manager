package com.foodflow.module.employee.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.foodflow.module.employee.dto.EmployeeLoginDTO;
import com.foodflow.module.employee.dto.EmployeeRegisterDTO;
import com.foodflow.module.employee.entity.Employee;
import com.foodflow.module.employee.vo.EmployeeLoginVO;
import com.foodflow.module.employee.vo.EmployeeRegisterVO;
import jakarta.validation.Valid;

public interface EmployeeService extends IService<Employee> {
    EmployeeRegisterVO register(@Valid EmployeeRegisterDTO employeeRegisterDTO);

    EmployeeLoginVO login(@Valid EmployeeLoginDTO employeeLoginDTO);
}
