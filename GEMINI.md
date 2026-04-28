# 🤖 Quy tắc Kỹ thuật & Nghiệp vụ (GEMINI.md)

File này lưu trữ các quyết định thiết kế và quy tắc kỹ thuật cụ thể của dự án WMS.

## 1. Quy chuẩn Code (Coding Standards)
- **Naming**: 
    - Variable/Method: `camelCase`.
    - Class: `PascalCase`.
    - Database Tables: `snake_case` với prefix theo module (ví dụ: `auth_`, `prd_`, `wh_`, `inv_`).
- **Exceptions**: Dùng `BussinessException` cho các lỗi nghiệp vụ kho, kèm thông báo Tiếng Việt rõ ràng.
- **Validation**: Mọi DTO Request phải được kiểm tra bằng `@Valid` và các annotation (`@NotNull`, `@Positive`,...).

## 2. Nghiệp vụ Kho (Core Business Logic)
### Cấu trúc Tồn kho (Inventory)
- **Unique Key**: Tồn kho được định danh duy nhất bởi bộ 3: `warehouse_id` + `product_id` + `zone_id`.
- **Số dư kép**:
    - `quantity`: Tổng số lượng thực tế trong kho.
    - `availableQuantity`: Số lượng sẵn sàng để xuất (đã trừ các phần giữ chỗ/hàng hỏng).
- **Trạng thái**: Mặc định là `ACTIVE`.

### Cơ chế Khóa (Locking & Concurrency)
- **Pessimistic Locking**: Sử dụng `findWithLockBy...` để thực hiện khóa hàng khi Nhập/Xuất/Chuyển.
- **Chống Deadlock (Transfer)**: 
    - Luôn so sánh chuỗi key `WH_ID:ZONE_ID:PROD_ID` giữa Nguồn và Đích.
    - Khóa bên có giá trị nhỏ hơn trước, sau đó mới khóa bên lớn hơn.

## 3. Giao tiếp & Hiệu năng
- **Kafka**: Sử dụng `InventoryEvent` để cập nhật tồn kho bất đồng bộ từ module Outbound (đặc biệt là với hàng Hot).
- **Redis**: Chứa số dư của các mặt hàng Hot Items để xử lý tranh chấp ở tốc độ cao (Atomic operations).

## 4. Cấu trúc Module (Modular Monolith)
- **auth**: Security, JWT, RBAC.
- **product**: Quản lý sản phẩm & Category.
- **warehouse**: Quản lý Warehouse & WarehouseZone.
- **inventory**: Xử lý stock, movement, transfer.
- **inbound/outbound**: Quy trình nghiệp vụ nhập xuất.

## 5. Danh sách Files quan trọng
- `InventoryService.java`: Nơi xử lý logic khóa và biến động kho.
- `OutboundService.java`: Xử lý đơn hàng, tích hợp Kafka/Redis.
- `ARCHITECTURE.md`: Mô tả kiến trúc tổng quát của hệ thống.
