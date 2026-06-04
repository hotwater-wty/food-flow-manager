package com.foodflow.module.diningsession.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodflow.common.result.Result;
import com.foodflow.module.diningsession.service.DiningSessionService;
import com.foodflow.module.diningsession.vo.DiningSessionVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/sessions")
public class UserDiningSessionController {
    private final DiningSessionService diningSessionService;

    /**
     * 用户查看当前开台会话
     */
    @GetMapping("/current")
    public Result<DiningSessionVO> getCurrentSession() {
        log.info("用户获取当前开台");
        DiningSessionVO diningSessionVO = diningSessionService.getCurrentSession();
        return Result.success(diningSessionVO);
    }
    
}
