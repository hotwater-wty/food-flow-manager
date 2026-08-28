package com.foodflow.module.notification.service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AdminNotificationService {
    private static final Duration TICKET_TTL = Duration.ofSeconds(60);
    private final Map<String, Ticket> tickets = new ConcurrentHashMap<>();
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public String issueTicket(Long employeeId) {
        String value = UUID.randomUUID().toString();
        tickets.put(value, new Ticket(employeeId, System.currentTimeMillis() + TICKET_TTL.toMillis()));
        return value;
    }

    public Long consumeTicket(String value) {
        Ticket ticket = tickets.remove(value);
        if (ticket == null || ticket.expiresAt < System.currentTimeMillis()) return null;
        return ticket.employeeId;
    }

    public SseEmitter connect(Long employeeId) {
        SseEmitter emitter = new SseEmitter(0L);
        SseEmitter previous = emitters.put(employeeId, emitter);
        if (previous != null) previous.complete();
        emitter.onCompletion(() -> emitters.remove(employeeId, emitter));
        emitter.onTimeout(() -> emitters.remove(employeeId, emitter));
        emitter.onError(error -> emitters.remove(employeeId, emitter));
        try {
            emitter.send(SseEmitter.event().name("connected").data(Map.of("employeeId", employeeId)));
        } catch (Exception error) {
            emitters.remove(employeeId, emitter);
            emitter.completeWithError(error);
        }
        return emitter;
    }

    public void publish(String eventName, Object data) {
        emitters.forEach((employeeId, emitter) -> {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (Exception error) {
                log.debug("SSE connection closed for employee {}", employeeId);
                emitters.remove(employeeId, emitter);
            }
        });
    }

    private record Ticket(Long employeeId, long expiresAt) {}
}
