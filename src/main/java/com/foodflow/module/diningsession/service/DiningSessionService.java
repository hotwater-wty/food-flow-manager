package com.foodflow.module.diningsession.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.foodflow.common.result.PageResult;
import com.foodflow.module.diningsession.dto.DiningSessionDTO;
import com.foodflow.module.diningsession.entity.DiningSession;
import com.foodflow.module.diningsession.vo.DiningSessionCloseVO;
import com.foodflow.module.diningsession.vo.DiningSessionVO;

public interface DiningSessionService extends IService<DiningSession> {

    /**
     * 获取当前用户会话
     * @return 会话VO
     */
    DiningSessionVO getCurrentSession();

    /**
     * 取消等待会话
     * @param sessionId 会话ID
     * @return 关闭会话VO
     */
    DiningSessionCloseVO cancelWaitingSession(Long sessionId);
    
    /**
     * 预约用户扫码到店
     * @param reservationId 预约ID
     * @param tableId 桌位ID
     * @return 会话VO
     */
    DiningSessionVO checkInReservation(Long reservationId, Long tableId);

    /**
     * 非预约用户扫码到店
     * @param tableId 桌位ID
     * @return 会话VO
     */
    DiningSessionVO checkInTable(Long tableId);

    /**
     * 获取会话列表
     * @param diningSessionDTO 会话查询DTO
     * @return 会话VO
     */
    PageResult<DiningSessionVO> getSessionList(DiningSessionDTO diningSessionDTO);

    /**
     * 获取会话详情
     * @param sessionId 会话ID
     * @return 会话VO
     */
    DiningSessionVO getSessionDetail(Long sessionId);

    /**
     * 关闭会话
     * @param sessionId 会话ID
     * @return 关闭会话VO
     */
    DiningSessionCloseVO closeSession(Long sessionId);
}
