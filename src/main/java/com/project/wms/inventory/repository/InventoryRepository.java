package com.project.wms.inventory.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.wms.inventory.entity.Inventory;

import jakarta.persistence.LockModeType;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    // tìm sản phẩm theo warehouseid và productid
    Optional<Inventory> findByWarehouseIdAndProductId(Long warehouseId, Long productId);

    // Khóa bi quan lock
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Inventory> findWithLockByWarehouseIdAndProductId(Long warehouseId, Long productId);

    // Lấy danh sách sản phẩm trong 1 kho cụ thể
    List<Inventory> findByWarehouseId(Long warehouseId);

    // Tính tổng kho của 1 sản phẩm trên các tất cả các kho
    @Query("SELECT SUM(i.quantity) FROM Inventory i WHERE i.product.id = :productId")
    Integer sumQuantityByProductId(@Param("productId") Long productId);

    // 4. Tìm các sản phẩm có số lượng tồn kho thấp hơn mức cảnh báo (Low Stock)
    List<Inventory> findByQuantityLessThan(int threshold);

    // 5. Kiểm tra nhanh xem sản phẩm đã từng được nhập vào kho đó chưa
    boolean existsByProductIdAndWarehouseId(Long productId, Long warehouseId);

    // tìm sản phẩm hot
    /**
     * Lấy danh sách hàng Hot để nạp vào Redis.
     * Sử dụng JOIN FETCH để giải quyết vấn đề N+1:
     * Lấy Inventory, Product và Warehouse chỉ trong 1 câu Query duy nhất.
     */
    @Query("SELECT i FROM Inventory i " +
            "JOIN FETCH i.product p " +
            "JOIN FETCH i.warehouse w " +
            "WHERE LOWER(p.name) LIKE LOWER(:keyword1) " +
            "OR LOWER(p.name) LIKE LOWER(:keyword2)")
    List<Inventory> findHotItems(@Param("keyword1") String keyword1, @Param("keyword2") String keyword2);

}
