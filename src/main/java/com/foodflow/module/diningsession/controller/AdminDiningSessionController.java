package com.foodflow.module.diningsession.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodflow.common.result.Result;
import com.foodflow.module.diningsession.service.DiningSessionService;
import com.foodflow.module.diningsession.vo.SessionCancelVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/sessions")
public class AdminDiningSessionController {

    private final DiningSessionService diningSessionService;

    /**
     * 店员取消等待中的用餐会话
     * @param sessionId 用餐会话ID
     * @return 成功结果
     */
    @PostMapping("{sessionId}/cancel")
    public Result<SessionCancelVO> cancelWaitingSession(@PathVariable Long sessionId) {
        SessionCancelVO sessionCancelVO = diningSessionService.cancelWaitingSession(sessionId);
        return Result.success(sessionCancelVO);
    }

}
