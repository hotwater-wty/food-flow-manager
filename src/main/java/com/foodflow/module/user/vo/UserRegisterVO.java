package com.foodflow.module.user.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRegisterVO {

    private Long userId;

    private String phone;

    private String nickname;

    private Integer status;
}
