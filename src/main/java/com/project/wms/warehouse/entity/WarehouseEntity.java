package com.project.wms.warehouse.entity;

import com.project.wms.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "wh_warehouse")
public class WarehouseEntity extends BaseEntity {

    @NotBlank(message = "Mã kho không được để trống")
    @Column(unique = true, nullable = false, length = 50)
    private String code;

    @NotBlank(message = "Tên kho không được để trống")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Địa chỉ kho không được để trống")
    @Column(columnDefinition = "TEXT")
    private String location;

    @Min(value = 0, message = "Dung lượng kho phải lớn hơn hoặc bằng 0")
    @Column(nullable = false)
    private int capacity;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true; // Trạng thái của kho để biết kho có hoạt động hay không
}
