package com.foodflow.module.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.foodflow.common.enums.EmployeeRoleEnum;
import com.foodflow.common.enums.EmployeeStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("employee")
public class Employee {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String phone;

    private String password;

    private EmployeeRoleEnum role;

    private String name;

    private EmployeeStatusEnum status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
