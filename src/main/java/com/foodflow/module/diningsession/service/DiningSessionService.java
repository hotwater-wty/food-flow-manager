package com.foodflow.module.diningsession.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.foodflow.module.diningsession.entity.DiningSession;
import com.foodflow.module.diningsession.vo.DiningSessionVO;

import org.springframework.stereotype.Service;

@Service
public interface DiningSessionService extends IService<DiningSession> {

    DiningSessionVO getCurrentSession();

    void cancelAdminSession(Long sessionId);
    
}
