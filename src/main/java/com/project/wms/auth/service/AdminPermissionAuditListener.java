package com.project.wms.auth.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.project.wms.auth.dto.AdminPermissionAuditEvent;
import com.project.wms.auth.entity.AdminPermissionAuditLog;
import com.project.wms.auth.repository.AdminPermissionAuditLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminPermissionAuditListener {

    private final AdminPermissionAuditLogRepository auditLogRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAdminPermissionAuditEvent(AdminPermissionAuditEvent event) {
        if (auditLogRepository.existsByEventId(event.eventId())) {
            log.info("[ADMIN_PERMISSION_AUDIT] Duplicate event ignored: {}", event.eventId());
            return;
        }

        try {
            auditLogRepository.save(AdminPermissionAuditLog.builder()
                    .eventId(event.eventId())
                    .action(event.action())
                    .adminId(event.adminId())
                    .adminUsername(event.adminUsername())
                    .targetUserId(event.targetUserId())
                    .targetUsername(event.targetUsername())
                    .warehouseId(event.warehouseId())
                    .warehouseName(event.warehouseName())
                    .permissionCode(event.permissionCode())
                    .permissionDescription(event.permissionDescription())
                    .reason(event.reason())
                    .build());
        } catch (Exception e) {
            log.error("[ADMIN_PERMISSION_AUDIT] Failed to save eventId={}", event.eventId(), e);
        }
    }
}
