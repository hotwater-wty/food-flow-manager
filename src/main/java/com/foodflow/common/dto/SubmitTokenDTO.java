package com.foodflow.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubmitTokenDTO {

    @NotBlank(message = "提交场景不能为空")
    private String scene;
}