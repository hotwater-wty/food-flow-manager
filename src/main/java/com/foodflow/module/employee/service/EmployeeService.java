package com.foodflow.module.employee.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.foodflow.module.employee.dto.EmployeeLoginDTO;
import com.foodflow.module.employee.dto.EmployeeRegisterDTO;
import com.foodflow.module.employee.entity.Employee;
import com.foodflow.module.employee.vo.EmployeeLoginVO;
import com.foodflow.module.employee.vo.EmployeeRegisterVO;
import com.foodflow.module.employee.vo.EmployeeVO;
import jakarta.validation.Valid;

public interface EmployeeService extends IService<Employee> {
    EmployeeRegisterVO register(@Valid EmployeeRegisterDTO employeeRegisterDTO);

    EmployeeLoginVO login(@Valid EmployeeLoginDTO employeeLoginDTO);

    List<EmployeeVO> getEmployeeList();

    EmployeeVO getEmployeeById(Long employeeId);

    EmployeeVO createEmployee(@Valid EmployeeRegisterDTO employeeRegisterDTO);

    void disableEmployee(Long employeeId);

    void enableEmployee(Long employeeId);
}
