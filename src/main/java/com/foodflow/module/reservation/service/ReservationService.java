package com.foodflow.module.reservation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.foodflow.module.reservation.dto.ReservationDTO;
import com.foodflow.module.reservation.entity.Reservation;
import com.foodflow.module.reservation.vo.ReservationCreateVO;

public interface ReservationService extends IService<Reservation>{

    ReservationCreateVO createReservation(ReservationDTO reservationDTO);
    
}
