package com.project.wms.inventory.entity;

import com.project.wms.auth.entity.User;
import com.project.wms.common.entity.BaseEntity;
import com.project.wms.inventory.enums.MovementType;
import com.project.wms.product.entity.Product;
import com.project.wms.warehouse.entity.WarehouseEntity;
import com.project.wms.warehouse.entity.WarehouseZone;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inv_stock_movements")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockMovement extends BaseEntity {

    @NotNull(message = "Sản phẩm không được để trống")
    Long productId;
    String productName;

    @NotNull(message = "Kho không được để trống")
    Long warehouseId;
    String warehouseName;

    @NotNull(message = "Khu vực không được để trống")
    Long zoneId;
    String zoneName;

    @NotNull(message = "Người dùng không được để trống")
    Long userId;
    String userName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementType movementType;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "reference_code")
    private String referenceCode;

}
