package com.foodflow.module.user.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor  
public class UserLoginVO {

    private Long userId;

    private String phone;

    private String nickname;

    private Integer status;

    private String token;
}
