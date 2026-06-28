package com.foodflow.module.diningsession.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodflow.common.result.PageResult;
import com.foodflow.common.result.Result;
import com.foodflow.module.diningsession.dto.DiningSessionDTO;
import com.foodflow.module.diningsession.service.DiningSessionService;
import com.foodflow.module.diningsession.vo.DiningSessionCloseVO;
import com.foodflow.module.diningsession.vo.DiningSessionVO;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/sessions")
@Tag(name = "管理端-堂食会话管理", description = "管理端堂食会话查询、取消等待和清台接口")
public class AdminDiningSessionController {

    private final DiningSessionService diningSessionService;

    /**
     * 店员取消等待中的用餐会话
     * 
     * @param sessionId 用餐会话ID
     * @return 成功结果
    */
    @PostMapping("{sessionId}/cancel")
    @Operation(summary = "取消等待中的堂食会话", description = "管理端取消尚未下单或等待中的堂食会话")
    public Result<DiningSessionCloseVO> cancelWaitingSession(
            @Parameter(description = "堂食会话ID", example = "1") @PathVariable Long sessionId) {
        DiningSessionCloseVO diningSessionCloseVO = diningSessionService.cancelWaitingSession(sessionId);
        return Result.success(diningSessionCloseVO);
    }

    /**
     * 店员获取所有用餐会话列表
     * 
     * @return 用餐会话列表
    */
    @GetMapping
    @Operation(summary = "查询堂食会话列表", description = "管理端按桌位、预约、会话或状态筛选堂食会话")
    public Result<PageResult<DiningSessionVO>> getSessionList(@ParameterObject DiningSessionDTO diningSessionDTO) {
        return Result.success(diningSessionService.getSessionList(diningSessionDTO));
    }

    /**
     * 店员获取用餐会话详情
     * 
     * @param sessionId 用餐会话ID
     * @return 用餐会话详情
    */
    @GetMapping("/{sessionId}")
    @Operation(summary = "查询堂食会话详情", description = "管理端根据会话ID查询堂食会话详情")
    public Result<DiningSessionVO> getSessionDetail(
            @Parameter(description = "堂食会话ID", example = "1") @PathVariable Long sessionId) {
        return Result.success(diningSessionService.getSessionDetail(sessionId));
    }

    /**
     * 管理员清台释放桌位
    */
    @PostMapping("/{sessionId}/close")
    @Operation(summary = "清台释放桌位", description = "管理端结束堂食会话并释放桌位")
    public Result<DiningSessionCloseVO> closeSession(
            @Parameter(description = "堂食会话ID", example = "1")
            @PathVariable @NotNull(message = "用餐会话ID不能为空") Long sessionId) {
        log.info("管理员清台释放桌位, sessionId: {}", sessionId);
        DiningSessionCloseVO diningSessionCloseVO = diningSessionService
                .closeSession(sessionId);
        return Result.success(diningSessionCloseVO);
    }

}
