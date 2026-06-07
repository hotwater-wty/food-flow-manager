package com.foodflow.module.employee.vo;

import com.foodflow.common.enums.EmployeeRoleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeVO {
    private Long employeeId;
    private String phone;
    private String name;
    private EmployeeRoleEnum role;
    private Integer status;
}
