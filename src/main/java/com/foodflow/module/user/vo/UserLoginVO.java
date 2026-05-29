package com.foodflow.module.user.vo;

import lombok.Data;

@Data
public class UserLoginVO {

    private Long userId;

    private String phone;

    private String nickname;

    private Integer status;

    private String token;
}
