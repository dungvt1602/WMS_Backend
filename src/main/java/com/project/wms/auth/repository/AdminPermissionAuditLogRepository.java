package com.project.wms.auth.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.wms.auth.entity.AdminPermissionAuditLog;

public interface AdminPermissionAuditLogRepository extends JpaRepository<AdminPermissionAuditLog, Long> {

    boolean existsByEventId(UUID eventId);
}
