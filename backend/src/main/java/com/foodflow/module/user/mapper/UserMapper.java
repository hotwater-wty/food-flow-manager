package com.foodflow.module.user.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodflow.module.user.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
