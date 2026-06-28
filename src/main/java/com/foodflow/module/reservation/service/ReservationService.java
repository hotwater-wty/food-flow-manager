package com.foodflow.module.reservation.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.foodflow.common.dto.PageQueryDTO;
import com.foodflow.common.result.PageResult;
import com.foodflow.module.reservation.dto.ReservationDTO;
import com.foodflow.module.reservation.entity.Reservation;
import com.foodflow.module.reservation.vo.ReservationCreateVO;
import com.foodflow.module.reservation.vo.ReservationVO;

public interface ReservationService extends IService<Reservation>{

    ReservationCreateVO createReservation(ReservationDTO reservationDTO);

    List<ReservationVO> getReservation();

    void cancelReservation(Long reservationId);

    PageResult<ReservationVO> getAllReservation(PageQueryDTO pageQueryDTO);

    ReservationVO getAdminReservationDetail(Long reservationId);

    void cancelAdminReservation(Long reservationId);

    ReservationVO getUserReservationDetail(Long reservationId);

}
