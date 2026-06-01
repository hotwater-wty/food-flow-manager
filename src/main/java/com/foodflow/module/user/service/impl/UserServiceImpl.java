package com.foodflow.module.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodflow.common.constant.JwtClaimConstants;
import com.foodflow.common.enums.LoginTypeEnum;
import com.foodflow.common.enums.UserStatusEnum;
import com.foodflow.common.exception.BusinessException;
import com.foodflow.common.utils.JwtUtil;
import com.foodflow.module.user.dto.UserLoginDTO;
import com.foodflow.module.user.dto.UserRegisterDTO;
import com.foodflow.module.user.entity.User;
import com.foodflow.module.user.mapper.UserMapper;
import com.foodflow.module.user.service.UserService;
import com.foodflow.module.user.vo.UserLoginVO;
import com.foodflow.module.user.vo.UserRegisterVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;

    @Override
    public UserLoginVO login(UserLoginDTO userLoginDTO) {
        String phone = userLoginDTO.getPhone();

        // 登录流程 1：先按手机号查出用户，不能把明文密码放到 SQL 条件里比较。
        User user = query()
                .eq("phone", phone)
                .one();

        // 登录流程 2：使用 BCrypt 的 matches 比较明文密码和数据库中的加密密码。
        if (user == null || !passwordEncoder.matches(userLoginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("手机号或密码错误");
        }
        if (user.getStatus() != UserStatusEnum.NORMAL) {
            throw new BusinessException("用户账号不可用");
        }

        // 登录流程 3：把后续鉴权需要的用户信息写入 JWT 载荷。
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put(JwtClaimConstants.USER_ID, user.getId());
        tokenMap.put(JwtClaimConstants.PHONE, user.getPhone());
        tokenMap.put(JwtClaimConstants.LOGIN_TYPE, LoginTypeEnum.USER.name());

        // 登录流程 4：生成 Token，返回给前端；前端后续请求放入 Authorization 请求头。
        String token = JwtUtil.generateToken(tokenMap);

        // 登录流程 5：封装登录响应，注意 userId 和 token 需要手动设置。
        UserLoginVO userLoginVO = toLoginVO(user);
        userLoginVO.setUserId(user.getId());
        userLoginVO.setToken(token);

        return userLoginVO;
    }

    @Override
    public UserRegisterVO register(UserRegisterDTO userRegisterDTO) {
        String phone = userRegisterDTO.getPhone();

        // 注册流程 1：手机号作为账号标识，先检查是否已经注册。
        User user = query().eq("phone", phone).one();
        if (user != null) {
            throw new BusinessException("用户已存在");
        }

        // 注册流程 2：密码只存 BCrypt 加密结果，不能存明文。
        String encodedPassword = passwordEncoder.encode(userRegisterDTO.getPassword());

        // 注册流程 3：未传昵称时，默认使用手机号作为昵称。
        if (userRegisterDTO.getNickname() == null || userRegisterDTO.getNickname().isEmpty()) {
            userRegisterDTO.setNickname(phone);
        }

        // 注册流程 4：组装用户实体，id 由数据库自增生成。
        user = User.builder()
                .phone(phone)
                .password(encodedPassword)
                .nickname(userRegisterDTO.getNickname())
                .status(UserStatusEnum.NORMAL)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        // 注册流程 5：保存后 MyBatis-Plus 会把数据库生成的主键 id 回填到 user 对象中。
        saveOrUpdate(user);

        // 注册流程 6：封装注册响应，避免把 password 返回给前端。
        return toRegisterVO(user);
    }

    private UserLoginVO toLoginVO(User user) {
        UserLoginVO userLoginVO = BeanUtil.copyProperties(user, UserLoginVO.class);
        userLoginVO.setStatus(user.getStatus().getCode());
        return userLoginVO;
    }

    private UserRegisterVO toRegisterVO(User user) {
        UserRegisterVO userRegisterVO = BeanUtil.copyProperties(user, UserRegisterVO.class);
        userRegisterVO.setUserId(user.getId());
        userRegisterVO.setStatus(user.getStatus().getCode());
        return userRegisterVO;
    }
}
