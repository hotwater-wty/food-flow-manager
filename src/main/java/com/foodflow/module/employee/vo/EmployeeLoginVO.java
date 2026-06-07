package com.foodflow.module.employee.vo;

import com.foodflow.common.enums.EmployeeRoleEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "员工登录响应数据")
public class EmployeeLoginVO {
    @Schema(description = "员工ID", example = "1")
    private Long employeeId;

    @Schema(description = "手机号", example = "13900139000")
    private String phone;

    @Schema(description = "员工姓名", example = "李四")
    private String name;

    @Schema(description = "员工角色：1-店员，2-店长", example = "1")
    private EmployeeRoleEnum role;

    @Schema(description = "员工状态：1-正常，2-禁用，3-离职", example = "1")
    private Integer status;

    @Schema(description = "JWT登录令牌", example = "eyJhbGciOiJIUzI1NiJ9.xxx")
    private String token;
}
