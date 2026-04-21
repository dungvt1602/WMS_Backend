package com.project.wms.inventory.entity;

import com.project.wms.common.entity.BaseEntity;
import com.project.wms.product.entity.Product;
import com.project.wms.warehouse.entity.WarehouseEntity;
import com.project.wms.warehouse.entity.WarehouseZone;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inv_stock_transfer_movements")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockTransferMovement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_warehouse_id", nullable = false)
    private WarehouseEntity fromWarehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_zone_id", nullable = true)
    private WarehouseZone fromZone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_warehouse_id", nullable = false)
    private WarehouseEntity toWarehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_zone_id", nullable = false)
    private WarehouseZone toZone;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "reference_code")
    private String referenceCode;

}
