package com.foodflow.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "防重复提交令牌请求参数")
public class SubmitTokenDTO {

    @Schema(
            description = "提交场景：create-reservation-创建预约，create-order-创建订单，open-session-扫码开台，clear-table-清台",
            example = "create-reservation",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "提交场景不能为空")
    private String scene;
}
