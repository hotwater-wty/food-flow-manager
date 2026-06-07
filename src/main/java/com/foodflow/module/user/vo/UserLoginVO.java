package com.foodflow.module.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor  
@Schema(description = "用户登录响应数据")
public class UserLoginVO {

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "用户昵称", example = "张三")
    private String nickname;

    @Schema(description = "用户状态：1-正常，2-禁用", example = "1")
    private Integer status;

    @Schema(description = "JWT登录令牌", example = "eyJhbGciOiJIUzI1NiJ9.xxx")
    private String token;
}
