package com.foodflow.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "防重复提交令牌响应数据")
public class SubmitTokenVO {

    @Schema(description = "一次性防重复提交令牌，提交写操作时通过 X-Submit-Token 请求头携带", example = "8f2d9a7c6b5e4d3c9a1b0f1234567890")
    private String token;

    @Schema(description = "令牌有效期，单位：秒", example = "300")
    private Long expiresInSeconds;
}
