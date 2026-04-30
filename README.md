# 📦 Hệ Thống Quản Lý Kho (WMS)

## 1. Giới thiệu

Đây là dự án backend mô phỏng hệ thống quản lý kho (Warehouse Management System – WMS).

Mục tiêu của dự án không chỉ dừng ở việc xây dựng CRUD cơ bản, mà tập trung vào việc rèn luyện các kiến thức nền tảng
của backend như:

- Quản lý transaction
- Xử lý concurrency
- Đảm bảo tính toàn vẹn dữ liệu
- Phân quyền người dùng (RBAC)
- Thiết kế kiến trúc Modular Monolith

Dự án được xây dựng với định hướng phát triển từ Junior lên Mid-level backend developer.

---

## 2. Phạm vi nghiệp vụ

**Hệ thống hỗ trợ các chức năng cơ bản:**

- Quản lý sản phẩm
- Đăng nhập / đăng ký người dùng
- Quản lý tồn kho theo từng kho
- Nhập kho (Inbound)
- Xuất kho (Outbound)
- Lưu lịch sử biến động tồn kho
- Xác thực và phân quyền người dùng

**Không bao gồm:**

- Giao diện frontend
- Microservices
- Hệ thống phân tán

---

## 3. Chức năng chính

### 3.1 Quản lý tồn kho

- Theo dõi số lượng tồn theo từng sản phẩm và kho
- Mỗi bản ghi tồn kho được quản lý theo cặp `(product_id, warehouse_id)`
- Không cho phép xuất âm kho (validate tại service layer và ràng buộc logic)
- Lưu log biến động tồn kho (Stock Movement Log)
- Đảm bảo toàn vẹn dữ liệu bằng:
    - Khóa ngoại (Foreign Key)
    - Unique constraint
    - Optimistic locking để tránh lost update

### 3.2 Nhập kho (Inbound)

- Tăng số lượng tồn kho tương ứng
- Ghi nhận một bản ghi Stock Movement loại `INBOUND`
- Phân quyền cập nhật trạng thái hóa đơn nhập kho:
    - Chỉ `Admin` hoặc `Staff` được phép cập nhật trạng thái
    - `Viewer` không có quyền thay đổi trạng thái hóa đơn
- Thực thi toàn bộ logic trong một transaction:
    - Cập nhật tồn kho
    - Lưu log biến động
    - Rollback toàn bộ nếu có lỗi xảy ra trong quá trình xử lý

### 3.3 Xuất kho (Outbound)

- Giảm số lượng tồn kho
- Kiểm tra đủ số lượng trước khi xuất
- Không cho phép xuất nếu tồn kho không đủ
- Ghi nhận một bản ghi Stock Movement loại `OUTBOUND`
- Permission lock cho nghiệp vụ trừ tồn kho:
    - Chỉ `Admin` hoặc `Staff` được phép thực hiện thao tác trừ tồn
    - Áp dụng lock tại thời điểm cập nhật tồn kho để tránh race condition khi nhiều request đồng thời
- Đảm bảo an toàn khi có nhiều request đồng thời:
    - Sử dụng optimistic locking để tránh lost update
    - Đặt ranh giới transaction tại service layer
    - Nếu có xung đột version: transaction bị rollback, client có thể retry
- Xử lý lỗi: nếu bất kỳ bước nào thất bại → toàn bộ transaction sẽ rollback

### 3.4 Xác thực & Phân quyền

**Xác thực:**

- Sử dụng JWT (JSON Web Token)
- Token có thời gian hết hạn
- Mật khẩu được mã hóa bằng BCrypt

**Phân quyền (RBAC):**

Hệ thống hỗ trợ 3 vai trò chính:

| Vai trò    | Quyền hạn                                                                  |
|------------|----------------------------------------------------------------------------|
| **Admin**  | Quản lý sản phẩm, quản lý kho, thực hiện nhập/xuất kho, quản lý người dùng |
| **Staff**  | Thực hiện nhập/xuất kho, xem tồn kho                                       |
| **Viewer** | Chỉ xem thông tin tồn kho                                                  |

**Phân quyền theo kho(RBAC + WarehouseId):**

- Người dùng chỉ được thao tác trên các kho được gán quyền
- Admin có thể được cấp quyền toàn bộ kho hoặc theo từng kho cụ thể
- Staff chỉ được nhập/xuất/xem tồn kho trong phạm vi warehouse được phân công
- Các API nhạy cảm cần kiểm tra đồng thời cả `role` và `warehouse scope` trước khi xử lý nghiệp vụ

Phân quyền được kiểm tra tại service hoặc security layer trước khi xử lý nghiệp vụ.

---

## 4. Công nghệ sử dụng

| Công nghệ          | Mục đích                    |
|--------------------|-----------------------------|
| Java / Spring Boot | Framework chính             |
| JPA / Hibernate    | ORM                         |
| PostgreSQL / MySQL | Cơ sở dữ liệu               |
| JWT                | Xác thực                    |
| Maven              | Build tool                  |
| Docker             | Container hóa               |
| Redis              | Cache / Distributed locking |
| Kafka              | Distributed                 |

---

## 5. Kiến trúc hệ thống

Hệ thống được thiết kế theo mô hình **Modular Monolith**:

- Triển khai dưới dạng một ứng dụng duy nhất
- Chia module theo domain: `auth`, `inventory`, `inbound`, `outbound`
- Phân tách rõ ràng service layer
- Sử dụng ràng buộc database để đảm bảo tính toàn vẹn dữ liệu

> Chi tiết hơn được mô tả trong file `ARCHITECTURE.md`.

---

## 6. Thiết kế cơ sở dữ liệu

Hệ thống được thiết kế theo hướng đảm bảo tính toàn vẹn dữ liệu, khả năng mở rộng và an toàn khi xử lý đồng thời.

### 6.1 Mô hình quản lý tồn kho

Tồn kho được quản lý theo từng cặp `(product, warehouse)`. Mỗi bản ghi tồn kho đại diện cho số lượng hiện tại của một
sản phẩm tại một kho cụ thể.

**Ràng buộc:**

- Unique constraint trên `(product_id, warehouse_id)`
- Không cho phép tồn kho âm ở tầng nghiệp vụ

**Lợi ích:**

- Dễ mở rộng sang nhiều kho
- Tránh trùng lặp dữ liệu tồn kho
- Đảm bảo tính nhất quán giữa sản phẩm và vị trí lưu trữ

### 6.2 Stock Movement Log

Mọi thay đổi về tồn kho đều được ghi lại trong bảng `stock_movement`.

**Bảng này lưu:**

- Loại biến động (`INBOUND`, `OUTBOUND`)
- Số lượng thay đổi
- Thời điểm thực hiện
- Người thực hiện

**Lợi ích:**

- Truy vết lịch sử thay đổi
- Phục vụ kiểm toán (audit)
- Có thể tái tạo lại trạng thái tồn kho nếu cần

### 6.3 Ràng buộc dữ liệu (Data Integrity)

Hệ thống sử dụng các cơ chế sau để đảm bảo toàn vẹn dữ liệu:

- Foreign key giữa các bảng liên quan
- Unique constraint để tránh trùng dữ liệu
- Validation ở tầng service trước khi ghi dữ liệu
- Transaction đảm bảo tính atomic

Việc kết hợp kiểm tra ở cả tầng ứng dụng và cơ sở dữ liệu giúp giảm rủi ro sai lệch dữ liệu.

### 6.4 Chiến lược xử lý đồng thời (Concurrency Control)

Để tránh hiện tượng **lost update** khi nhiều request cùng cập nhật tồn kho:

- Sử dụng **optimistic locking** (version column)
- Mỗi lần cập nhật tồn kho sẽ kiểm tra version hiện tại
- Nếu có xung đột, transaction sẽ rollback

Cách tiếp cận này phù hợp với hệ thống có tần suất ghi trung bình và giúp:

- Giữ hiệu năng tốt hơn so với locking cứng
- Đảm bảo dữ liệu không bị ghi đè ngoài ý muốn

> ERD được đặt tại: `/docs/erd.png`

---

## 7. Chiến lược xử lý Concurrency

Ngoài optimistic locking, hệ thống cũng có thể sử dụng:

- **Pessimistic locking** (`SELECT FOR UPDATE`)
- **Atomic database update** để tránh race condition
- **Distributed locking** (Redis) trong môi trường scale-out
- **Queue-based processing** thông qua message broker (Kafka/RabbitMQ)
- **Inventory reservation pattern** cho các nghiệp vụ cần đảm bảo tính nhất quán cao

> Sequence diagram được đặt trong thư mục `/docs`.

---



Tài liệu secrets chi tiết: [docs/security-secrets.md](docs/security-secrets.md)

## Deploy (OKE + Jenkins + Vault)

Mục tiêu production hiện tại:

- Deploy backend WMS lên Oracle Kubernetes Engine (OKE).
- Dùng Jenkins để build/test/deploy tự động.
- Quản lý secrets bằng Vault (không hardcode trong repo).

Checklist triển khai theo phase:

- Phase 1: Dockerfile + health check + push GitHub.
- Phase 2: Setup OKE cluster + Jenkins VM + kubectl.
- Phase 3: Viết và apply Kubernetes manifests.
- Phase 4: Hoàn thiện CI/CD pipeline + webhook.
- Phase 5: Cài Vault, migrate secrets từ K8s Secret sang Vault.
- Phase 6: Kết nối frontend (Vercel) và test E2E.

Frontend deployment:

- Frontend được deploy trên Vercel.
- Cấu hình `VITE_API_BASE_URL` trỏ về backend đã deploy.
- Bật CORS ở backend để cho phép frontend gọi API.

Runbook chi tiết:

- [deploy.md](deploy.md)

## 9. Cài đặt & chạy dự án

```bash
# 1. Clone repository
git clone https://github.com/<your-org>/<your-repo>.git

# 2. Vào thư mục dự án
cd wms

# 3. Cấu hình Environment Variables cho môi trường dev
# JWT_SECRET, JWT_EXPIRATION, DB_USERNAME, DB_PASSWORD,
# GOOGLE_GENAI_API_KEY, GOOGLE_GENAI_PROJECT_ID

# 4. Khởi động hạ tầng local bằng Docker Compose
docker compose up -d

# 5. Chạy ứng dụng với profile dev
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Hoặc nếu đã cài Maven global:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Ứng dụng chạy tại: [http://localhost:8080](http://localhost:8080)

## 10. Trọng tâm học tập

Dự án tập trung vào các vấn đề backend cốt lõi:

- Isolation level và transaction
- Data consistency
- Concurrency control
- Thiết kế service layer rõ ràng
- Tư duy mở rộng hệ thống

---

## 11. Nhật ký năng cấp hệ thống (Chặng đường WMS Doanh Nghiệp)

Dự án đã trải qua 7 Phase để từng bước giải quyết các bài toán hóc búa nhất của Production:

1. **Core Entities**: Inbound, Outbound, Inventory + Stock Movement Log (Audit Trail) hoàn chỉnh.
2. **Concurrency Mức độ CSDL**: Áp dụng Optimistic Locking (`@Version`) để khóa cứng hiện tượng Lost Update.
3. **Data Security**: Xây dựng màng lưới Security RBAC (Phân quyền JWT).
4. **Performance**: Thiết lập Redis Cache xua tan nỗi lo cho các bảng Read-Heavy (Product/Warehouse).
5. **Base Code Standards**: Tích hợp Global Exception Handler bóc tách lỗi DTO Validation sang chuẩn Response.
6. **Data Integrity (No Hard Delete)**: Phá lệnh xóa cứng. Rào chắn SQL bằng cơ chế Soft Delete.
7. **Scale to Enterprise Level (Mới nhất)**:
    - **Luồng Ảo (Java 21 Virtual Threads)**: Quét sạch Tomcat Thread truyền thống, hỗ trợ chịu tải lên tới 10,000+
      Concurrent Requests (chuẩn thiết kế ứng dụng System Design) với RAM tiết kiệm nhất.
    - **DB Connection Pool Manager**: Cứu hộ DB không thở gấp bằng hàng rào Hikari (Max-Pool-Size).
    - **Spring Data Pagination**: Bảo vệ tài nguyên, cấm tiệt truy vấn `List<T> findAll()`, chuẩn hóa xuất hàng loạt
      bằng `Page<T>` để chống Memory Leak.

---

## 12. Hướng phát triển trong tương lai

- [ ] Phân tán hóa Module (Microservices DB per Service)
- [ ] Thêm idempotency cho tác vụ Outbound
- [ ] Ghi log bất đồng bộ bằng Kafka hoặc RabbitMQ
- [ ] Trình quản lý sơ đồ Database Version Control (Sử dụng Flyway)





