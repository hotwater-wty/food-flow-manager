package com.foodflow.module.reservation.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodflow.module.reservation.entity.Reservation;

@Mapper
public interface ReservationMapper extends BaseMapper<Reservation> {
    
}
