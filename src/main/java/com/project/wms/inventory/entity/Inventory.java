package com.project.wms.inventory.entity;

import com.project.wms.common.entity.BaseEntity;
import com.project.wms.product.entity.Product;
import com.project.wms.warehouse.entity.WarehouseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "inv_inventory", uniqueConstraints = @UniqueConstraint(columnNames = { "warehouse_id", "product_id" }))
public class Inventory extends BaseEntity {

    @NotNull(message = "Kho không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private WarehouseEntity warehouse;

    @NotNull(message = "Sản phẩm không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    @Min(value = 0, message = "Số lượng không được âm")
    private int quantity; // số lượng thực tế trong kho

    @Column(nullable = false)
    @Min(value = 0, message = "Số lượng không được âm")
    private int availableQuantity; // số lượng có sẵn = quantity - reservedQuantity

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String status;
    private String note;

    @Version
    private Long version;

}
