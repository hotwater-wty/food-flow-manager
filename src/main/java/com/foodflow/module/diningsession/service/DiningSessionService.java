package com.foodflow.module.diningsession.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.foodflow.module.diningsession.dto.DiningSessionDTO;
import com.foodflow.module.diningsession.entity.DiningSession;
import com.foodflow.module.diningsession.vo.DiningSessionCloseVO;
import com.foodflow.module.diningsession.vo.DiningSessionVO;
import com.foodflow.module.diningsession.vo.SessionCancelVO;

import java.util.List;


public interface DiningSessionService extends IService<DiningSession> {

    DiningSessionVO getCurrentSession();

    SessionCancelVO cancelWaitingSession(Long sessionId);
    
    DiningSessionVO checkInReservation(Long reservationId, Long tableId);

    DiningSessionVO checkInTable(Long tableId);

    List<DiningSessionVO> getSessionList(DiningSessionDTO diningSessionDTO);

    DiningSessionVO getSessionDetail(Long sessionId);

    DiningSessionCloseVO closeSession(Long sessionId);
}
