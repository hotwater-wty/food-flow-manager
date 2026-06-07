package com.foodflow.module.table.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TableVO {
    private Long tableId;
    private String tableNo;
    private Integer capacity;
    private String locationDesc;
    private Integer status;
    private Long currentSessionId;
}
