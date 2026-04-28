package com.project.wms.inventory.entity;

import com.project.wms.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inv_inventory_log", uniqueConstraints = @UniqueConstraint(name = "uq_order_product", columnNames = {
        "order_code", "product_id" }))
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventoryLog extends BaseEntity {

    @Column(name = "order_code", nullable = false)
    private String orderCode;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "movement_type", nullable = false)
    private String movementType; // OUTBOUND_HOT, OUTBOUND_COLD

}
