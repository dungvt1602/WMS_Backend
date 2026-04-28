package com.project.wms.inventory.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.wms.inventory.entity.Inventory;

import jakarta.persistence.LockModeType;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    // tìm sản phẩm theo warehouseid và productid và zoneid (nếu không có zone thì
    // tự động tìm hàng chưa gán zone)
    @Query("SELECT i FROM Inventory i WHERE i.warehouse.id = :warehouseId AND i.product.id = :productId AND ((:zoneId IS NULL AND i.zone IS NULL) OR i.zone.id = :zoneId)")
    Optional<Inventory> findByWarehouseIdAndProductIdAndZoneId(@Param("warehouseId") Long warehouseId,
                                                               @Param("productId") Long productId, @Param("zoneId") Long zoneId);

    // Khóa bi quan lock
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.warehouse.id = :warehouseId AND i.product.id = :productId AND ((:zoneId IS NULL AND i.zone IS NULL) OR i.zone.id = :zoneId)")
    Optional<Inventory> findWithLockByWarehouseIdAndProductIdAndZoneId(@Param("warehouseId") Long warehouseId,
                                                                       @Param("productId") Long productId, @Param("zoneId") Long zoneId);

    // Lấy danh sách sản phẩm trong 1 kho cụ thể
    List<Inventory> findByWarehouseId(Long warehouseId);

    Page<Inventory> findByWarehouseId(Long warehouseId, Pageable pageable);

    Page<Inventory> findByProductId(Long productId, Pageable pageable);

    Page<Inventory> findByWarehouseIdAndProductId(Long warehouseId, Long productId, Pageable pageable);

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

    // Trừ hàng (dùng trong outbound) automic
    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Inventory i
            SET i.quantity = i.quantity - :quantity, i.availableQuantity = i.availableQuantity - :quantity
            WHERE i.id = :id AND i.availableQuantity >= :quantity
            """)
    int remakeStock(@Param("id") Long id, @Param("quantity") int quantity);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Inventory i
            SET i.quantity = i.quantity + :quantity, i.availableQuantity = i.availableQuantity + :quantity
            WHERE i.id = :id AND i.quantity > 0
            """)
    int addStock(@Param("id") Long id, @Param("quantity") int quantity);

    @Query("""
            SELECT COUNT(DISTINCT i.product.id)
            FROM Inventory i
            WHERE (:warehouseId IS NULL OR i.warehouse.id = :warehouseId)
            """)
    long countDistinctProducts(@Param("warehouseId") Long warehouseId);

    @Query("""
            SELECT COUNT(DISTINCT i.warehouse.id)
            FROM Inventory i
            WHERE (:warehouseId IS NULL OR i.warehouse.id = :warehouseId)
            """)
    long countDistinctWarehouses(@Param("warehouseId") Long warehouseId);

    @Query("""
            SELECT COALESCE(SUM(i.quantity), 0)
            FROM Inventory i
            WHERE (:warehouseId IS NULL OR i.warehouse.id = :warehouseId)
            """)
    long sumQuantity(@Param("warehouseId") Long warehouseId);

    @Query("""
            SELECT COALESCE(SUM(i.availableQuantity), 0)
            FROM Inventory i
            WHERE (:warehouseId IS NULL OR i.warehouse.id = :warehouseId)
            """)
    long sumAvailableQuantity(@Param("warehouseId") Long warehouseId);

    @Query("""
            SELECT COUNT(i)
            FROM Inventory i
            WHERE i.quantity < :threshold
              AND (:warehouseId IS NULL OR i.warehouse.id = :warehouseId)
            """)
    long countLowStockItems(@Param("warehouseId") Long warehouseId, @Param("threshold") int threshold);

}
