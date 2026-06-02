package com.foodflow.module.diningsession.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiningSessionDTO {

    @NotNull(message = "预约ID不能为空")
    private Long reservationId;

    @NotNull(message = "餐桌ID不能为空")
    private Long tableId;

}
