package com.project.wms.outbound.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.project.wms.common.entity.BaseEntity;
import com.project.wms.common.enums.OrderStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "out_outbound_order")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OutboundOrder extends BaseEntity {

    @NotBlank(message = "Ma xuat kho khong duoc de trong")
    @Column(unique = true)
    private String orderCode;

    @NotNull(message = "Thoi gian xuat kho khong duoc de trong")
    private LocalDateTime orderDateTime;

    @NotNull(message = "Trang thai don hang khong duoc de trong")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(columnDefinition = "TEXT")
    private String note;

    @NotBlank(message = "Khach hang khong duoc de trong")
    @Column(nullable = false)
    private String customerName;

    @OneToMany(mappedBy = "outboundOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OutboundOrderItems> outboundOrderItems;

}
