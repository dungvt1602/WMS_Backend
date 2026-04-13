# Đưa hệ thống WMS đạt chuẩn Enterprise Logic, Kiến Trúc & High-Concurrency (Phase 7)

Dự án WMS đã hoạt động trơn tru về mặt Base. Trong Phase này, chúng ta sẽ trang bị "Vũ khí hạng nặng" của Java 21 **(Virtual Threads - Luồng ảo)** mà các Bank và Core Enterprise đang áp dụng, kèm theo đó là tinh chỉnh dữ liệu lớn (Pagination) và Data Integrity. Flyway sẽ được để lại sau cùng.

## User Review Required
Bạn hãy đọc cấu trúc mới của bản kế hoạch này. Khi dùng **Virtual Threads** với quy mô nghìn request/s, hệ thống đã thực sự lột xác thành chuẩn Doanh nghiệp. Nếu đồng ý, hãy bấm **"Approve"**.

## Proposed Changes

### 1. Kích hoạt Virtual Threads (Chuẩn Core Banking / High-load WMS)
*   **Điểm nghẽn hiện tại:** Tomcat mặc định duy trì một Thread-pool 200 threads. Nếu 200 nhân viên cùng truy vấn kho (I/O call DB/Redis lâu), hệ thống bị treo chờ.
*   **[MODIFY]** `application.properties`: Khai báo tính năng native của Java 21 + Spring Boot 3.2+: `spring.threads.virtual.enabled=true`. 
*   **Hiệu ứng:** Ứng dụng WMS của bạn giờ đây có thể gánh được hàng chục nghìn luồng concurrent requests (bằng Virtual Threads siêu nhẹ của JVM) mà RAM tăng không đáng kể. 

### 2. Tối ưu Hiệu Năng & Chống sập RAM (Pagination cơ bản)
*   **[MODIFY]** `WarehouseService.java` & `WarehouseController.java`: Cập nhật `getAllWarehouses()` đổi sang tham số `Pageable` của Spring Data.
*   **[MODIFY]** `ProductService.java` & `ProductController.java`: Cập nhật `getAllProducts()` áp dụng phân trang (Pagination).

### 3. An Toàn Dữ Liệu: Xoá Bỏ Hard Delete (Xóa Cứng) nguy hiểm
*   **[MODIFY]** `WarehouseService.java`: Cấm việc dùng JPA `.delete()`. Đã lên Enterprise, không được phép xóa vật lý (Hard delete) các dữ liệu đã có liên kết khóa ngoại. Đổi lại hoàn toàn bằng Logic Soft Delete qua cờ `isActive`.
*   **[MODIFY]** `ProductService.java`: Tương tự, Product cũng cần đảm bảo trạng thái Active/Inactive, không xóa cứng nếu đã tồn tại log xuất/nhập, chống lệch sổ sách.

### 4. Chuẩn hóa Validation Error (Global Controller Advice)
*   **[MODIFY]** `com/project/wms/common/exception/HandleRuntimeException.java`: Bổ sung `@ExceptionHandler` với class `MethodArgumentNotValidException` để tự động bẻ các lỗi Binding Data của DTO (VD thiếu `@NotBlank`) xuống `ApiResponse`, giúp cho App/Web Frontend dễ đọc và hiển thị lỗi trên UI.

### 5. Cập nhật Tài liệu Kiến Trúc Doanh Nghiệp (Docs)
*   **[MODIFY]** `README.md` & `ARCHITECTURE.md`: Document toàn bộ cấu trúc kiến trúc Java 21 Virtual Threads (Loom), định lý phân trang, chống Soft Delete. Để CV/Project đọc lên là chuẩn mảng System Design của Bank/Enterprise.

## Open Questions
- Spring Boot với Virtual Thread sẽ xử lý rất nhanh, nhưng nhược điểm là nó chia kết nối xuống DB quá nhiều nếu bị burst request. Bạn có muốn cấu hình luôn Pool Size của HikariCP (Connection Pool của DB) để giới hạn khả năng DB bị chết ngộp khi nhận chục nghìn thread ảo tải một lúc không? (Ví dụ giới hạn Connection Pool = 50).

## Verification Plan
1. **Virtual Thread Log**: Chạy log Spring Boot, nếu nó in ra Tomcat sử dụng `tomcat-handler-vt` (virtual thread) thay vì `http-nio-8080-exec` tức là thành công 100%.
2. **Lỗi Xóa Dữ liêu**: Chạy Postman để xóa thực thể. Thấy code báo "Không hỗ trợ xóa cứng" -> Đạt.
3. **Pagination Test**: Dùng tham số `?page=0&size=5` ở Postman sinh ra JSON Page -> Đạt.
