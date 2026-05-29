package com.foodflow.module.table.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class TableDTO {

    @NotBlank(message = "桌号不能为空")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "桌号只能包含字母和数字")
    private String tableNo;

    @NotNull(message = "容量不能为空")
    @Min(value = 1, message = "容量必须大于0")
    private Integer capacity;
    
    private String locationDesc;
}
