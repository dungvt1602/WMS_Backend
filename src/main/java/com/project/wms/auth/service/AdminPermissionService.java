package com.project.wms.auth.service;

import com.project.wms.auth.dto.PermissionWarehouseDTO.*;
import com.project.wms.auth.entity.AuthPermission;
import com.project.wms.auth.entity.PermissionWarehouse;
import com.project.wms.auth.entity.User;
import com.project.wms.auth.repository.AuthPermisionRepository;
import com.project.wms.auth.repository.PermissionWarehouseRepository;
import com.project.wms.auth.repository.UserRepository;
import com.project.wms.auth.utils.SecurityUtils;
import com.project.wms.warehouse.entity.WarehouseEntity;
import com.project.wms.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminPermissionService {

    private final PermissionWarehouseRepository permissionWarehouseRepository;
    private final UserRepository userRepository;
    private final WarehouseRepository warehouseRepository;
    private final AuthPermisionRepository authPermisionRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // Grant
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public WarehousePermissionResponse grant(GrantWarehousePermissionRequest request) {
        //Tim thong tin user, warehouseId, permissionCode
        Long adminId = SecurityUtils.getCurrentUserId();

        User user = userRepository.findById(request.userId()).orElseThrow(
                () -> new RuntimeException("Khong tim thay nguoi dung phu hop " + request.userId())
        );

        WarehouseEntity warehouse = warehouseRepository.findById(request.warehouseId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy warehouse: " + request.warehouseId()));

        AuthPermission permission = authPermisionRepository.findByCode(request.permissionCode())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy permission: " + request.permissionCode()));

        try {
            //Tao entity
            PermissionWarehouse permision = PermissionWarehouse.builder()
                    .user(user)
                    .warehouse(warehouse)
                    .permission(permission)
                    .grantBy(adminId)
                    .build();
            //Luu entity vao repository
            PermissionWarehouse newper = permissionWarehouseRepository.save(permision);
            log.info("Permission warehouse succesfully granted: " + request.userId());
            return mapToResponse(newper);

        } catch (Exception e) {
            // UNIQUE constraint — đã cấp quyền này rồi, trả về idempotent
            log.info("[PERMISSION] Already exists, skipping: userId={} warehouseId={} permission={}",
                    request.userId(), request.warehouseId(), request.permissionCode());

            return permissionWarehouseRepository
                    .findByUserIdAndWarehouseIdAndPermissionCode(request.userId(), request.warehouseId(), request.permissionCode())
                    .map(this::mapToResponse)
                    .orElseThrow();
        }

    }

    // ─────────────────────────────────────────────────────────────────────────
    // Revoke
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public void revoke(RevokeWarehousePermissionRequest request) {
        Long adminId = SecurityUtils.getCurrentUserId();

        PermissionWarehouse existing = permissionWarehouseRepository
                .findByUserIdAndWarehouseIdAndPermissionCode(request.userId(), request.warehouseId(), request.permissionCode())
                .orElseThrow(() -> new RuntimeException(
                        String.format("Quyền %s của user %d tại warehouse %d không tồn tại",
                                request.permissionCode(), request.userId(), request.warehouseId())));

        permissionWarehouseRepository.delete(existing);

        log.info("[PERMISSION] REVOKED: userId={} warehouseId={} permission={} by adminId={}",
                request.userId(), request.warehouseId(), request.permissionCode(), adminId);
    }


    // List
    // ─────────────────────────────────────────────────────────────────────────

    public List<WarehousePermissionResponse> getByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("Không tìm thấy user: " + userId);
        }

        return permissionWarehouseRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<WarehousePermissionResponse> getByWarehouse(Long warehouseId) {
        if (!warehouseRepository.existsById(warehouseId)) {
            throw new RuntimeException("Không tìm thấy warehouse: " + warehouseId);
        }

        return permissionWarehouseRepository.findAllByWarehouseId(warehouseId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Mapper
    // ─────────────────────────────────────────────────────────────────────────

    private WarehousePermissionResponse mapToResponse(PermissionWarehouse p) {
        return new WarehousePermissionResponse(
                p.getId(),
                p.getUser().getId(),
                p.getUser().getUsername(),
                p.getWarehouse().getId(),
                p.getWarehouse().getName(),
                p.getPermission().getCode(),
                p.getPermission().getDescription(),
                p.getGrantBy(),
                p.getGrantAt()
        );
    }


}
