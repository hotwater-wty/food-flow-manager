package com.foodflow.module.diningsession.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiningSessionVO {
    private Long sessionId;
    private String sessionNo;
    private Long tableId;
    private String tableNo;
    private Integer sessionStatus;
    private Integer tableStatus;
}
