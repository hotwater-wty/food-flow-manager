package com.foodflow.module.diningsession.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodflow.common.result.Result;
import com.foodflow.module.diningsession.dto.DiningSessionDTO;
import com.foodflow.module.diningsession.service.DiningSessionService;
import com.foodflow.module.diningsession.vo.DiningSessionVO;
import com.foodflow.module.diningsession.vo.SessionCancelVO;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;


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

    /**
     * 店员获取所有用餐会话列表
     * @return 用餐会话列表
     */
    @GetMapping
    public Result<List<DiningSessionVO>> getSessionList(DiningSessionDTO diningSessionDTO) {
        return Result.success(diningSessionService.getSessionList(diningSessionDTO));
    }
    
    /**
     * 店员获取用餐会话详情
     * @param sessionId 用餐会话ID
     * @return 用餐会话详情
     */
    @GetMapping("/{sessionId}")
    public Result<DiningSessionVO> getSessionDetail(@PathVariable Long sessionId) {
        return Result.success(diningSessionService.getSessionDetail(sessionId));
    }

    /**
     * 管理员清台释放桌位
     */
    @PostMapping("/{sessionId}/close")
    public Result<DiningSessionVO> closeSession(
            @PathVariable @NotNull(message = "用餐会话ID不能为空") Long sessionId) {
        log.info("管理员清台释放桌位, sessionId: {}", sessionId);
        DiningSessionVO diningSessionVO = diningSessionService
                .closeSession(sessionId);
        return Result.success(diningSessionVO);
    }

}
