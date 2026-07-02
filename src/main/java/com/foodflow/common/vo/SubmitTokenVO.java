package com.foodflow.common.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubmitTokenVO {

    private String token;

    private Long expiresInSeconds;
}