package com.foodflow.module.diningsession.vo;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SessionCancelVO {
    private LocalDateTime closeTime;
    private Long closeEmployeeId;
    private LocalDateTime updateTime;
}
