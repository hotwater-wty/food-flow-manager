package com.foodflow.module.diningsession.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodflow.module.diningsession.service.DiningSessionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/sessions")
public class AdminDiningSessionController {
    private final DiningSessionService diningSessionService;

}
