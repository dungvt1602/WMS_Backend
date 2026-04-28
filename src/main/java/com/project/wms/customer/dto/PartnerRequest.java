package com.project.wms.customer.dto;

import com.project.wms.customer.entity.CustomerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PartnerRequest(
    @NotBlank(message = "Mã đối tác không được để trống")
    String code,

    @NotBlank(message = "Tên đối tác không được để trống")
    String name,

    @NotNull(message = "Loại đối tác không được để trống")
    CustomerType type,

    String phone,
    String email,
    String address
) {}
