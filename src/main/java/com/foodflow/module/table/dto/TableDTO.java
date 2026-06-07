package com.foodflow.module.table.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "桌位请求参数")
public class TableDTO {

    @Schema(description = "桌号，只能包含字母和数字", example = "A01", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "桌号不能为空")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "桌号只能包含字母和数字")
    private String tableNo;

    @Schema(description = "可容纳人数", example = "4", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "容量不能为空")
    @Min(value = 1, message = "容量必须大于0")
    private Integer capacity;

    @Schema(description = "桌位位置描述", example = "大厅靠窗")
    private String locationDesc;
}
