package com.foodflow.module.reservation.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodflow.module.reservation.dto.ReservationDTO;
import com.foodflow.module.reservation.entity.Reservation;
import com.foodflow.module.reservation.mapper.ReservationMapper;
import com.foodflow.module.reservation.service.ReservationService;
import com.foodflow.module.reservation.vo.ReservationCreateVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> implements ReservationService {@Override
    public ReservationCreateVO createReservation(ReservationDTO reservationDTO) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createReservation'");
    }
    
}
