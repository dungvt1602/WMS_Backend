package com.project.wms.auth.service;

import com.project.wms.auth.dto.PermissionCode;
import com.project.wms.auth.entity.PermissionWarehouse;
import com.project.wms.auth.entity.User;
import com.project.wms.auth.repository.PermissionWarehouseRepository;
import com.project.wms.common.exception.AccessDeniedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PermissionWarehouseService {

    private final PermissionWarehouseRepository permissionWarehouseRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // API công khai — gọi từ service layer
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Check quyền của user tại 1 warehouse cụ thể.
     * <p>
     * ROLE_ADMIN → bypass, không cần check warehouse.
     * ROLE_STAFF / ROLE_VIEWER → phải có permission trong auth_user_warehouse_permission.
     *
     */
    public void checkUserPermissions(Long userId, Long warehouseId, PermissionCode permissionCode) {

        Boolean permission = permissionWarehouseRepository.existsByUserIdAndWarehouseIdAndPermissionCode(
                userId, warehouseId, permissionCode.name()
        );
        if (!permission) {
            log.warn("[ACCESS] DENIED: userId={} warehouseId={} permission={}",
                    userId, warehouseId, permissionCode.name());
            throw new AccessDeniedException(permissionCode.name(), userId, warehouseId);
        }
        log.debug("[ACCESS] GRANTED: userId={} warehouseId={} permission={}",
                userId, warehouseId, permissionCode.name());

    }

    /**
     * Check quyền của user trên NHIỀU warehouse cùng lúc.
     * Dùng cho createOrder / completeOrder khi order có items từ nhiều kho.
     * <p>
     * Ví dụ: order có 3 items từ warehouse 1, 2, 3
     * → user phải có OUTBOUND_CREATE tại cả 3 kho
     *
     * @throws AccessDeniedException ngay khi phát hiện warehouse đầu tiên bị thiếu quyền
     */
    public void checkPermissionByListWarehouseId(Long userId,
                                                 Collection<Long> list,
                                                 PermissionCode permissionCode) {
        //Lọc list id thành set duy nhất dđể ko bị trùng id
        Set<Long> warehouseIds = list.stream().collect(Collectors.toSet());
        for (Long warehouseId : warehouseIds) {
            boolean check = permissionWarehouseRepository.existsByUserIdAndWarehouseIdAndPermissionCode(
                    userId, warehouseId, permissionCode.name()
            );
            if (!check) {
                log.error("[PERMISSION] {} không có quyền {} tại warehouseid: {}", userId, permissionCode.name(), warehouseId);
                throw new AccessDeniedException(permissionCode.name(), userId, warehouseId);

            }
        }

        log.debug("[PERMISSION] GRANTED all {} warehouses: userId={} permission={}",
                warehouseIds.size(), userId, permissionCode.name());
    }

    /**
     * Check quyền trên 2 warehouse — dùng cho transferStock.
     * User phải có quyền ở cả fromWarehouse và toWarehouse.
     */
    public void checkPermssionForTransfer(
            Long userId,
            Long fromWarehouseId,
            Long toWarehouseId,
            PermissionCode permission
    ) {
        checkPermissionByListWarehouseId(
                userId,
                List.of(fromWarehouseId, toWarehouseId),
                permission
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Check user có ROLE_ADMIN không.
     * ROLE_ADMIN bypass toàn bộ warehouse permission check.
     */


}
