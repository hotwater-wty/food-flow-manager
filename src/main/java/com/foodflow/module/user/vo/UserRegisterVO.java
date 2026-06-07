package com.foodflow.module.user.vo;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterVO {

    private Long userId;

    private String phone;

    private String nickname;

    private Integer status;
}
