package com.foodflow.module.diningsession.vo;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiningSessionCloseVO {
    private Long sessionId;
    private String sessionNo;
    private Long tableId;
    private String tableNo;
    private Integer sessionStatus;
    private Integer tableStatus;
    private LocalDateTime closeTime;
    private Long closeEmployeeId;
}
