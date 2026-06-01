package com.foodflow.module.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.foodflow.module.user.dto.UserLoginDTO;
import com.foodflow.module.user.dto.UserRegisterDTO;
import com.foodflow.module.user.entity.User;
import com.foodflow.module.user.vo.UserLoginVO;
import com.foodflow.module.user.vo.UserRegisterVO;

public interface UserService extends IService<User> {

    /**
     * 用户登录
     * @param userLoginDTO 登录DTO
     * @return 用户VO
     */
    UserLoginVO login(UserLoginDTO userLoginDTO);

    /**
     * 用户注册
     * @param userRegisterDTO 注册DTO
     * @return 用户VO
     */
    UserRegisterVO register(UserRegisterDTO userRegisterDTO);
}
