package com.project.wms.auth.repository;

import com.project.wms.auth.entity.PermissionWarehouse;
import com.project.wms.auth.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PermissionWarehouseRepository extends JpaRepository<PermissionWarehouse, Long> {
    /**
     * Query cốt lõi — dùng trong WarehouseAccessService để check quyền.
     * "User X có permission code Y tại warehouse Z không?"
     */

    @Query("""
            SELECT COUNT(i) > 0
            FROM PermissionWarehouse i
            WHERE i.user.id = :userId AND
                i.warehouse.id = :warehouseId
                 AND  i.permission.code = :permissionCode
            """)
    boolean existsByUserIdAndWarehouseIdAndPermissionCode(
            @Param("userId") Long userId,
            @Param("warehouseId") Long warehouseId,
            @Param("permissionCode") String permissionCode
    );

    /**
     * Lấy tất cả quyền của 1 user tại 1 warehouse.
     * Dùng cho API GET /admin/users/{userId}/warehouse-permissions.
     */
    @Query("""
            SELECT p FROM PermissionWarehouse p 
                   JOIN FETCH User u 
                    JOIN FETCH WarehouseEntity w 
                           WHERE p.user.id = :userId
                                   ORDER BY p.warehouse.id, p.grantAt
            """)
    List<PermissionWarehouse> findByUserId(@Param("userId") Long userId);


    /**
     * Tìm record cụ thể để revoke.(Xoa quyen cua nguoi dung)
     */
    @Query("""
             SELECT i
                       FROM PermissionWarehouse i
                        WHERE i.user.id = :userId AND
                            i.warehouse.id = :warehouseId
                             AND  i.permission.code = :permissionCode
            """)
    Optional<PermissionWarehouse> findByUserIdAndWarehouseIdAndPermissionCode(
            @Param("userId") Long userId,
            @Param("warehouseId") Long warehouseId,
            @Param("permissionCode") String permissionCode
    );

    /**
     * Lấy tất cả user có quyền tại 1 warehouse.
     * Dùng để admin xem danh sách nhân viên của 1 kho.
     */
    @Query("""
            SELECT p FROM PermissionWarehouse p
                        JOIN FETCH User u 
                                    JOIN FETCH WarehouseEntity w
            WHERE p.warehouse.id = :warehouseId
                        GROUP BY p.user.id, p.grantAt
            """)
    List<PermissionWarehouse> findAllByWarehouseId(@Param("warehouseId") Long warehouseId);


    Long user(User user);
}
