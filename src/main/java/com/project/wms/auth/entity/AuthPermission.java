package com.project.wms.auth.entity;

import com.project.wms.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;


/**
 * Master data cho các loại quyền trong hệ thống.
 * Seed 1 lần khi khởi động, không thay đổi runtime.
 *
 * Các code hiện tại:
 *   INBOUND_CREATE, INBOUND_COMPLETE,
 *   OUTBOUND_CREATE, OUTBOUND_COMPLETE,
 *   INVENTORY_VIEW, INVENTORY_ADJUST
 */

@Entity
@Table(name = "auth_permission")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthPermission extends BaseEntity {
    /**
     * Mã quyền duy nhất — dùng để check trong WarehouseAccessService.
     * Ví dụ: "OUTBOUND_CREATE", "INVENTORY_VIEW"
     */
    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "description", length = 255)
    private String description;
}
