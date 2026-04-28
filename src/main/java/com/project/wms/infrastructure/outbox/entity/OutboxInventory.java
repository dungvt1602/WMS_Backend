package com.project.wms.infrastructure.outbox.entity;

import com.project.wms.common.entity.BaseEntity;
import com.project.wms.infrastructure.outbox.dto.OutboxStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "outbox_inventory", indexes = {
        // Index quan trọng — dispatcher query WHERE status='PENDING' ORDER BY
        // created_at
        // Nếu thiếu index này thì full scan mỗi 5 giây
        @Index(name = "idx_outbox_status_created", columnList = "status, created_at")
})
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OutboxInventory extends BaseEntity {

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "pay_load", nullable = false, columnDefinition = "TEXT", length = 200)
    private String payload;

    // dung de biet topic gi de gui cho kafka
    @Column(name = "topic", nullable = false, length = 100)
    private String topic;

    // Dung de trach idempotency va atomic
    @Column(name = "order_code", unique = true, nullable = false)
    private String orderCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private OutboxStatus status = OutboxStatus.PENDING;

    // Đếm số lần retry để alert nếu vượt ngưỡng
    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private int retryCount = 0;

    // Lưu lý do lỗi gần nhất để debug
    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    public void markRetried(String errorMess) {
        this.retryCount++;
        this.lastError = errorMess;
        if (retryCount >= 5) {
            this.status = OutboxStatus.DEAD;
        }
    }

}
