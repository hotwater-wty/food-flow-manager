package com.foodflow.module.notification.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 管理端实时通知服务。
 *
 * <p>业务模块只依赖此接口，不直接依赖 SSE 的内存连接实现，便于后续替换为
 * Redis Pub/Sub 或其他多实例事件分发方案。</p>
 */
public interface AdminNotificationService {
    /** 为员工签发短时、一次性消费的 SSE ticket。 */
    String issueTicket(Long employeeId);

    /** 消费 SSE ticket；不存在、过期或已消费时返回 {@code null}。 */
    Long consumeTicket(String value);

    /** 为员工建立或替换一条 SSE 长连接。 */
    SseEmitter connect(Long employeeId);

    /** 向当前在线管理端连接广播业务事件。 */
    void publish(String eventName, Object data);

    /** 在当前事务提交后广播事件；无事务时立即广播。 */
    void publishAfterCommit(String eventName, Object data);
}
