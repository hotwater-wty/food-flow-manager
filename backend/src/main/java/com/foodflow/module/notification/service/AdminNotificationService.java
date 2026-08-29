package com.foodflow.module.notification.service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AdminNotificationService {
    private static final Duration TICKET_TTL = Duration.ofSeconds(60);
    private final Map<String, Ticket> tickets = new ConcurrentHashMap<>();
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final LongSupplier currentTimeMillis;

    public AdminNotificationService() {
        this(System::currentTimeMillis);
    }

    AdminNotificationService(LongSupplier currentTimeMillis) {
        this.currentTimeMillis = currentTimeMillis;
    }

    public String issueTicket(Long employeeId) {
        long now = currentTimeMillis.getAsLong();
        tickets.entrySet().removeIf(entry -> entry.getValue().expiresAt < now);
        String value = UUID.randomUUID().toString();
        tickets.put(value, new Ticket(employeeId, now + TICKET_TTL.toMillis()));
        return value;
    }

    public Long consumeTicket(String value) {
        Ticket ticket = tickets.remove(value);
        if (ticket == null || ticket.expiresAt < currentTimeMillis.getAsLong()) return null;
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

    /** Publish only after a surrounding business transaction has committed. */
    public void publishAfterCommit(String eventName, Object data) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.isSynchronizationActive()) {
            publish(eventName, data);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                publish(eventName, data);
            }
        });
    }

    private record Ticket(Long employeeId, long expiresAt) {}
}
