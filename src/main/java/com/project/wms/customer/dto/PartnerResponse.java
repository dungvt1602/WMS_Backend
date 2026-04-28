package com.project.wms.customer.dto;

import com.project.wms.customer.entity.CustomerType;
import java.time.LocalDateTime;

public record PartnerResponse(
    Long id,
    String code,
    String name,
    CustomerType type,
    String phone,
    String email,
    String address,
    boolean active,
    LocalDateTime createdAt
) {}
