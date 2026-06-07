package com.foodflow.common.result;

import java.io.Serializable;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "分页响应结果")
public class PageResult<T> implements Serializable {
    @Schema(description = "总记录数", example = "100")
    private Long total;

    @Schema(description = "当前页码", example = "1")
    private Integer pageNo;

    @Schema(description = "每页数量", example = "10")
    private Integer pageSize;

    @Schema(description = "当前页数据列表")
    private List<T> records;
}
