package com.project.wms.outbound.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.project.wms.common.entity.BaseEntity;
import com.project.wms.common.enums.OrderStatus;
import com.project.wms.customer.entity.Customer;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = true)
    private Customer customer;

    @OneToMany(mappedBy = "outboundOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OutboundOrderItems> outboundOrderItems;

    @Column(unique = true)
    private String requestId; // Dùng để chống trùng lặp request khi hệ thống phân tán (Idempotency)

}
