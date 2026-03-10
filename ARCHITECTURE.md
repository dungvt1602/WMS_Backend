# 🏗️ Kiến Trúc Hệ Thống WMS (ARCHITECTURE)

## 1. Kiến trúc tổng quát (High-Level Architecture)

Hệ thống WMS được thiết kế theo mô hình **Modular Monolith**.

Trong mô hình này, toàn bộ hệ thống được triển khai dưới dạng một ứng dụng duy nhất, nhưng code được chia thành nhiều module theo domain nhằm đảm bảo tính tách biệt logic.

**Các module chính của hệ thống:**

- Auth
- Product
- Warehouse
- Inventory
- Inbound
- Outbound

**Luồng dữ liệu tổng quát:**

```
Client
  |
Controller Layer
  |
Service Layer (Module Domain)
  |
Repository
  |
PostgreSQL Database
```

Mỗi module có service riêng và chỉ giao tiếp với module khác thông qua service interface, giúp giữ ranh giới domain rõ ràng.

### Lý do chọn Modular Monolith

Thay vì triển khai Microservices ngay từ đầu, hệ thống sử dụng Modular Monolith vì các lý do sau:

- **Đơn giản hóa hạ tầng:** chỉ cần deploy một service
- **Giảm độ phức tạp DevOps:** không cần service discovery, API gateway
- **Tránh network latency** giữa các service
- **Dễ refactor domain** trong giai đoạn đầu của dự án
- **Phù hợp với team nhỏ** (2–5 người)

> Thiết kế module tách biệt giúp hệ thống có thể tách thành Microservices trong tương lai nếu cần.

---

## 2. Phân ranh giới Module (Module Boundaries & Communication)

Một nguyên tắc quan trọng của kiến trúc là giữ ranh giới module rõ ràng. Mỗi module chịu trách nhiệm cho một domain logic riêng.

**Ví dụ:**

| Module | Trách nhiệm |
|--------|-------------|
| Auth | Quản lý xác thực và phân quyền |
| Inventory | Quản lý tồn kho |
| Inbound | Xử lý nhập kho |
| Outbound | Xử lý xuất kho |

### Dependency Rule

Các module chỉ được phép phụ thuộc theo hướng sau:

```
Inbound  ----\
              > Inventory
Outbound ----/

Inventory -> Product
Inventory -> Warehouse
```

**Quy tắc:**
- Module nghiệp vụ (Inbound/Outbound) được phép gọi Inventory
- Inventory **không** được gọi ngược lại Inbound hoặc Outbound

Điều này giúp tránh **circular dependency**.

### Cơ chế giao tiếp giữa các module

Các module giao tiếp thông qua Service Layer.

**Ví dụ:**
```
InboundService
  -> InventoryService.adjustStock()
```

Controller của module không được gọi trực tiếp service của module khác.

**Luồng chuẩn:**
```
Controller → Service (module hiện tại)
               ↓
         Service (module khác)
```

Cách này giúp:
- Giữ logic nghiệp vụ ở service
- Tránh coupling giữa controller các module

---

## 3. Chiến lược dữ liệu (Data Management Strategy)

Hệ thống sử dụng một **database chung (PostgreSQL)** cho toàn bộ ứng dụng. Mặc dù database chung, nhưng schema được thiết kế để phản ánh ranh giới domain.

### Database Naming Strategy

Tên bảng được đặt theo **prefix module**:

```
auth_user
auth_role

prd_product

wh_warehouse

inv_inventory
inv_stock_movement
```

Cách đặt tên này giúp:
- Dễ nhận biết domain của bảng
- Hỗ trợ tách database khi chuyển sang microservices

### Data Integrity

Trong giai đoạn Modular Monolith, hệ thống sử dụng Foreign Key và Database constraints.

**Ví dụ:**
```sql
inv_inventory.product_id   -> prd_product.id
inv_inventory.warehouse_id -> wh_warehouse.id
```

Điều này giúp đảm bảo:
- Không tồn tại tồn kho cho sản phẩm không tồn tại
- Dữ liệu luôn nhất quán

> Trong trường hợp chuyển sang microservices, các ràng buộc này sẽ được thay bằng application-level validation.

### Redis Caching Policy

Redis được sử dụng cho các dữ liệu **read-heavy**.

**Ví dụ:**
- Danh sách sản phẩm
- Danh sách warehouse

**Mục tiêu:**
- Giảm số lần truy vấn database
- Tăng tốc response API

Cache được invalidated khi dữ liệu thay đổi.

---

## 4. Xử lý đồng thời & Giao dịch (Concurrency & Transactions)

Quản lý tồn kho là một bài toán nhạy cảm về đồng thời (concurrency). Hệ thống áp dụng nhiều cơ chế để đảm bảo dữ liệu chính xác.

### Transaction Management

Các nghiệp vụ quan trọng như nhập kho và xuất kho được thực hiện trong transaction.

`@Transactional` chỉ được đặt ở **Service Layer**.

**Ví dụ — `OutboundService.exportStock()`:**

```
1. Đọc tồn kho
2. Kiểm tra điều kiện (không xuất âm kho)
3. Cập nhật tồn kho
4. Ghi stock movement
```

> Nếu một bước thất bại → rollback toàn bộ transaction.

### Optimistic Locking

Hệ thống sử dụng **optimistic locking** để tránh vấn đề **Lost Update**.

Bảng tồn kho có trường `version`. Khi cập nhật:

```sql
UPDATE inventory
SET quantity = ?, version = version + 1
WHERE id = ? AND version = ?
```

Nếu version thay đổi do transaction khác → update thất bại → retry.

Cách này giúp xử lý tình huống: *Hai nhân viên cùng xuất kho một sản phẩm cùng lúc.*

### Idempotency

Một vấn đề phổ biến là client gửi request trùng lặp.

**Ví dụ:** Người dùng bấm nút "Xuất kho" 2 lần.

Để tránh xử lý trùng, hệ thống sử dụng **idempotency strategy**:

- Mỗi request có `request_id`
- Server kiểm tra request đã xử lý chưa
- Nếu request đã tồn tại → trả kết quả cũ

> Điều này đảm bảo: **Một hành động nghiệp vụ chỉ được thực thi một lần duy nhất.**

---

## 5. Hạ tầng & Triển khai (Infrastructure & Deployment)

Hệ thống được thiết kế để dễ dàng triển khai bằng **Docker**.

**Các service chính:**

- Application (Spring Boot)
- PostgreSQL
- Redis

### Docker Compose

`docker-compose.yml` chạy toàn bộ hệ thống:

```yaml
services:
  ├─ app
  ├─ postgres
  └─ redis
```

**Lý do tách container:**
- Database và cache có vòng đời riêng
- Dễ scale độc lập trong tương lai

### Environment Configuration

Cấu hình môi trường được quản lý thông qua **environment variables**.

```env
DB_HOST=
DB_PORT=
DB_USERNAME=
DB_PASSWORD=

REDIS_HOST=
REDIS_PORT=

JWT_SECRET=
```

Các biến môi trường được load thông qua `application.yml`.