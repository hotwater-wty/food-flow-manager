package com.foodflow.common.context;

import com.foodflow.common.enums.EmployeeRoleEnum;
import com.foodflow.common.enums.LoginTypeEnum;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginInfo {

    /**
     * 普通用户 ID。
     * loginType = USER 时有值。
     */
    private Long userId;

    /**
     * 员工 ID。
     * loginType = EMPLOYEE 时有值。
     */
    private Long employeeId;

    /**
     * 登录主体类型：USER / EMPLOYEE。
     */
    private LoginTypeEnum loginType;

    /**
     * 员工角色：STAFF / MANAGER。
     * 只有 loginType = EMPLOYEE 时有值。
     */
    private EmployeeRoleEnum employeeRole;
}