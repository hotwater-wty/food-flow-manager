package com.foodflow.module.table.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TableVO {
    private Long tableId;
    private String tableNo;
    private Integer capacity;
    private String locationDesc;
    private Integer status;
    private Long currentSessionId;
}
