package com.foodflow.module.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "用户注册响应数据")
public class UserRegisterVO {

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "用户昵称", example = "张三")
    private String nickname;

    @Schema(description = "用户状态：1-正常，2-禁用", example = "1")
    private Integer status;
}
