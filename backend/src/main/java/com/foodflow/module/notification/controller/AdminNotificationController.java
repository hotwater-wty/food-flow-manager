package com.foodflow.module.notification.controller;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.foodflow.common.context.LoginContext;
import com.foodflow.common.result.Result;
import com.foodflow.module.notification.service.AdminNotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/notifications")
public class AdminNotificationController {
    private final AdminNotificationService notificationService;

    @PostMapping("/ticket")
    public Result<Map<String, String>> issueTicket() {
        return Result.success(Map.of("ticket", notificationService.issueTicket(LoginContext.getEmployeeId())));
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam String ticket) {
        Long ticketEmployeeId = notificationService.consumeTicket(ticket);
        if (ticketEmployeeId == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "SSE ticket 无效或已过期");
        }
        return notificationService.connect(ticketEmployeeId);
    }

    @PostMapping("/test")
    public Result<Void> testEvent() {
        notificationService.publish("test-notification", Map.of("message", "SSE test event"));
        return Result.success();
    }
}
