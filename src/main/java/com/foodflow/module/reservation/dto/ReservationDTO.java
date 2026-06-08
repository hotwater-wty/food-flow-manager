package com.foodflow.module.reservation.dto;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "创建预约请求参数")
public class ReservationDTO {
    @Schema(description = "预约桌位ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "桌位ID不能为空")
    private Long tableId;

    @Schema(description = "预约人数", example = "4", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "人数不能为空")
    @Min(value = 1, message = "人数必须大于等于1人")
    private Integer peopleCount;

    @Schema(description = "预约时间，格式：yyyy-MM-dd HH:mm:ss", example = "2026-06-08 18:30:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "预约时间不能为空")
    @Future(message = "预约时间必须在当前时间之后")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:SS")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:SS")
    private LocalDateTime reserveTime;
}
