package com.project.wms.auth.dto;

/**
 * Enum các permission code trong hệ thống.
 * Dùng thay vì hardcode string để tránh typo.
 *
 * Cách dùng trong service:
 *   warehouseAccessService.assertHasWarehousePermission(
 *       userId, warehouseId, PermissionCode.OUTBOUND_CREATE.name()
 *   );
 */

public enum PermissionCode {

    //Tao cac object final khong the thay doi
    INBOUND_CREATE   ("Tạo phiếu nhập kho"),
    INBOUND_COMPLETE ("Duyệt phiếu nhập kho"),
    OUTBOUND_CREATE  ("Tạo phiếu xuất kho"),
    OUTBOUND_COMPLETE("Duyệt phiếu xuất kho"),
    INVENTORY_VIEW   ("Xem tồn kho"),
    INVENTORY_ADJUST ("Điều chỉnh tồn kho");

    private final String description;

    PermissionCode(String description){
        this.description = description;
    }

    public String getDescription(){
        return description;
    }

}
