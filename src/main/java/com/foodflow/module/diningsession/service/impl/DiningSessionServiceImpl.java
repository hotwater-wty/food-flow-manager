package com.foodflow.module.diningsession.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodflow.module.diningsession.entity.DiningSession;
import com.foodflow.module.diningsession.mapper.DiningSessionMapper;
import com.foodflow.module.diningsession.service.DiningSessionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DiningSessionServiceImpl extends ServiceImpl<DiningSessionMapper, DiningSession> implements DiningSessionService {
        
}
