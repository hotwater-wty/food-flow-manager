package com.foodflow.module.user.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodflow.common.result.Result;
import com.foodflow.module.user.dto.UserLoginDTO;
import com.foodflow.module.user.dto.UserRegisterDTO;
import com.foodflow.module.user.service.UserService;
import com.foodflow.module.user.vo.UserLoginVO;
import com.foodflow.module.user.vo.UserRegisterVO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Slf4j
@RestController
@RequestMapping("/api/user/auth")
@RequiredArgsConstructor
public class UserAuthController {

    private final UserService userService;

    // 通过@Valid注解并配合全局异常处理器，实现参数校验和异常处理，可处理非空问题

    @PostMapping("/register")
    public Result<UserRegisterVO> register(@Valid @RequestBody UserRegisterDTO userRegisterDTO) {
        log.info("用户注册: {}", userRegisterDTO);
        UserRegisterVO userVO = userService.register(userRegisterDTO);
        return Result.success(userVO);
    }

    @PostMapping("/login")
    public Result<UserLoginVO> login(@Valid @RequestBody UserLoginDTO userLoginDTO) {
        log.info("用户登录: {}", userLoginDTO);
        UserLoginVO userLoginVO = userService.login(userLoginDTO);
        return Result.success(userLoginVO);
    }
}
