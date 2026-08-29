package com.foodflow.module.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class AdminNotificationServiceTest {
    @AfterEach
    void clearSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

    @Test
    void ticketCanOnlyBeConsumedOnce() {
        AdminNotificationService service = new AdminNotificationService(() -> 1_000L);
        String ticket = service.issueTicket(7L);
        assertThat(service.consumeTicket(ticket)).isEqualTo(7L);
        assertThat(service.consumeTicket(ticket)).isNull();
    }

    @Test
    void expiredTicketIsRejected() {
        AtomicLong now = new AtomicLong(1_000L);
        AdminNotificationService service = new AdminNotificationService(now::get);
        String ticket = service.issueTicket(7L);
        now.addAndGet(60_001L);
        assertThat(service.consumeTicket(ticket)).isNull();
    }

    @Test
    void rollbackDoesNotPublishRegisteredEvent() {
        AdminNotificationService service = spy(new AdminNotificationService());
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);

        service.publishAfterCommit("new-order", Map.of("orderId", 1));
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(sync -> sync.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));

        verify(service, never()).publish("new-order", Map.of("orderId", 1));
    }
}
