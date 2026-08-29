package com.foodflow.module.notification.service.impl;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.foodflow.module.notification.service.AdminNotificationService;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AdminNotificationServiceImpl implements AdminNotificationService {
    /** Ticket 只用于建立连接，不承担可靠消息投递，因此有效期保持很短。 */
    private static final Duration TICKET_TTL = Duration.ofSeconds(60);

    /** 单实例阶段按员工保存连接；同一员工的新连接会替换旧连接。 */
    private final Map<String, Ticket> tickets = new ConcurrentHashMap<>();
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final LongSupplier currentTimeMillis;

    public AdminNotificationServiceImpl() {
        this(System::currentTimeMillis);
    }

    /** 为测试注入时间来源，避免等待真实 60 秒验证过期逻辑。 */
    public AdminNotificationServiceImpl(LongSupplier currentTimeMillis) {
        this.currentTimeMillis = currentTimeMillis;
    }

    /** 清理过期 ticket 后生成随机值，并绑定签发员工。 */
    @Override
    public String issueTicket(Long employeeId) {
        long now = currentTimeMillis.getAsLong();
        tickets.entrySet().removeIf(entry -> entry.getValue().expiresAt < now);
        String value = UUID.randomUUID().toString();
        tickets.put(value, new Ticket(employeeId, now + TICKET_TTL.toMillis()));
        return value;
    }

    /**
     * 使用 remove 实现一次性消费：并发请求中只有一个请求能拿到 ticket。
     */
    @Override
    public Long consumeTicket(String value) {
        Ticket ticket = tickets.remove(value);
        if (ticket == null || ticket.expiresAt < currentTimeMillis.getAsLong()) return null;
        return ticket.employeeId;
    }

    /** 建立无限期 SSE 连接，并保证每名员工最多保留一条当前连接。 */
    @Override
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

    /** 向在线连接发送命名事件；发送失败时移除失效连接。 */
    @Override
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

    /**
     * 业务写事务存在时注册 afterCommit 回调，避免事务回滚后仍通知管理端。
     * 无事务调用（例如开发测试事件）则直接发送。
     */
    @Override
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
