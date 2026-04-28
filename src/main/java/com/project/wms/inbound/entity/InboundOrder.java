package com.project.wms.inbound.entity;

import java.time.LocalDate;
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
@Table(name = "inb_inbound_order")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundOrder extends BaseEntity {

    @NotBlank(message = "Mã phiếu ko được để trống")
    @Column(unique = true)
    private String orderNumber; // Mã phiếu

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @NotNull(message = "Thời gian nhập hàng không được để trống")
    private LocalDateTime orderDateTime;

    @Column(columnDefinition = "TEXT")
    private String note;

    @OneToMany(mappedBy = "inboundOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InboundOrderItems> inboundOrderItems;

}
