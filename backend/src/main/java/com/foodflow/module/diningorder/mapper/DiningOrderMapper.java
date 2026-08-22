package com.foodflow.module.diningorder.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodflow.module.diningorder.entity.DiningOrder;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DiningOrderMapper extends BaseMapper<DiningOrder> {
    
}
