package com.project.wms.warehouse.entity;

import com.project.wms.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Table(name = "wh_zone")
public class WarehouseZone extends BaseEntity {

    @NotBlank(message = "Mã khu/vị trí không được để trống")
    @Column(unique = true, nullable = false, length = 50)
    private String code;

    @NotBlank(message = "Tên khu/vị trí không được để trống")
    @Column(nullable = false, length = 100)
    private String name;

    @NotNull(message = "Kho chứa không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private WarehouseEntity warehouse;

    @Min(value = 0, message = "Sức chứa tối đa phải lớn hơn hoặc bằng 0")
    @Column(nullable = false)
    private int capacity;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
