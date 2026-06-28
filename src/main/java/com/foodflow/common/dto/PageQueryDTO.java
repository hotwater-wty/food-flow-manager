package com.foodflow.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Schema(description = "分页查询参数")
public class PageQueryDTO {

    @Schema(description = "页码", example = "1")
    @Min(value = 1, message = "页码必须大于等于1")
    private Integer pageNo = 1;

    @Schema(description = "每页数量", example = "10")
    @Min(value = 1, message = "每页数量必须大于等于1")
    @Max(value = 500, message = "每页数量不能超过500")
    private Integer pageSize = 10;
}
