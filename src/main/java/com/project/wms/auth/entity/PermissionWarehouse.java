package com.project.wms.auth.entity;

import com.project.wms.common.entity.BaseEntity;
import com.project.wms.warehouse.entity.WarehouseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "aut_permission_perwarehouse" ,
uniqueConstraints = @UniqueConstraint(name = "uq_warehouse_userid" ,
columnNames = {"user_id", "warehouse_id", "permission_id"}) ,
indexes = {@Index(name = "idx_warehouse_userid" , columnList = "warehouse_id , user_id") ,
        // Index để query: "Ai có quyền tại Warehouse Y?"
        @Index(name = "idx_uwp_warehouse", columnList = "warehouse_id") }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionWarehouse extends BaseEntity {

    /**
     * User được cấp quyền.
     * LAZY vì không cần load toàn bộ User khi check permission.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id" , nullable = false)
    private User user;

    /**
     * Kho mà quyền này áp dụng.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id" , nullable = false)
    private WarehouseEntity warehouse;

    /**
     * Quyen cua nguoi do.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id" , nullable = false)
    private AuthPermission permission;

    //Luu thong tin cua nguoi gan quyen
    @Column(name = "grant_by")
    private long grantBy;

    @Column(name = "granted_at", nullable = false)
    @Builder.Default
    private LocalDateTime grantAt = LocalDateTime.now();

}
