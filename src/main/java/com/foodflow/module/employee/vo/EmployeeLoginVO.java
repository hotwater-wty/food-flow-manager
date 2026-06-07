package com.foodflow.module.employee.vo;

import com.foodflow.common.enums.EmployeeRoleEnum;
import lombok.Data;

@Data
public class EmployeeLoginVO {
    private Long employeeId;

    private String phone;

    private String name;

    private EmployeeRoleEnum role;

    private Integer status;

    private String token;
}
