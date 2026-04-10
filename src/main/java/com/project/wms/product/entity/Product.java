package com.project.wms.product.entity;

import java.math.BigDecimal;

import com.project.wms.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "products")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product extends BaseEntity {

    @NotBlank(message = "Sản phẩm không được để trống")
    @Column(unique = true, nullable = false)
    private String name;

    @NotBlank(message = "Mã sản phẩm không được để trống")
    @Column(unique = true, nullable = false)
    private String sku;

    @NotBlank(message = "Mô tả không được để trống")
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Giá không được để trống")
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal price;

    @Min(value = 0, message = "Số lượng không được âm")
    @Column(nullable = false)
    private int stock;

    @NotBlank(message = "Đơn vị tính không được để trống")
    private String unit;
}
