# ðŸ“¦ Há»‡ Thá»‘ng Quáº£n LÃ½ Kho (WMS)

## 1. Giá»›i thiá»‡u

ÄÃ¢y lÃ  dá»± Ã¡n backend mÃ´ phá»ng há»‡ thá»‘ng quáº£n lÃ½ kho (Warehouse Management System â€“ WMS).

Má»¥c tiÃªu cá»§a dá»± Ã¡n khÃ´ng chá»‰ dá»«ng á»Ÿ viá»‡c xÃ¢y dá»±ng CRUD cÆ¡ báº£n, mÃ  táº­p trung vÃ o viá»‡c rÃ¨n luyá»‡n cÃ¡c kiáº¿n thá»©c ná»n táº£ng cá»§a backend nhÆ°:

- Quáº£n lÃ½ transaction
- Xá»­ lÃ½ concurrency
- Äáº£m báº£o tÃ­nh toÃ n váº¹n dá»¯ liá»‡u
- PhÃ¢n quyá»n ngÆ°á»i dÃ¹ng (RBAC)
- Thiáº¿t káº¿ kiáº¿n trÃºc Modular Monolith

Dá»± Ã¡n Ä‘Æ°á»£c xÃ¢y dá»±ng vá»›i Ä‘á»‹nh hÆ°á»›ng phÃ¡t triá»ƒn tá»« Junior lÃªn Mid-level backend developer.

---

## 2. Pháº¡m vi nghiá»‡p vá»¥

**Há»‡ thá»‘ng há»— trá»£ cÃ¡c chá»©c nÄƒng cÆ¡ báº£n:**

- Quáº£n lÃ½ sáº£n pháº©m
- ÄÄƒng nháº­p / Ä‘Äƒng kÃ½ ngÆ°á»i dÃ¹ng
- Quáº£n lÃ½ tá»“n kho theo tá»«ng kho
- Nháº­p kho (Inbound)
- Xuáº¥t kho (Outbound)
- LÆ°u lá»‹ch sá»­ biáº¿n Ä‘á»™ng tá»“n kho
- XÃ¡c thá»±c vÃ  phÃ¢n quyá»n ngÆ°á»i dÃ¹ng

**KhÃ´ng bao gá»“m:**

- Giao diá»‡n frontend
- Microservices
- Há»‡ thá»‘ng phÃ¢n tÃ¡n

---

## 3. Chá»©c nÄƒng chÃ­nh

### 3.1 Quáº£n lÃ½ tá»“n kho

- Theo dÃµi sá»‘ lÆ°á»£ng tá»“n theo tá»«ng sáº£n pháº©m vÃ  kho
- Má»—i báº£n ghi tá»“n kho Ä‘Æ°á»£c quáº£n lÃ½ theo cáº·p `(product_id, warehouse_id)`
- KhÃ´ng cho phÃ©p xuáº¥t Ã¢m kho (validate táº¡i service layer vÃ  rÃ ng buá»™c logic)
- LÆ°u log biáº¿n Ä‘á»™ng tá»“n kho (Stock Movement Log)
- Äáº£m báº£o toÃ n váº¹n dá»¯ liá»‡u báº±ng:
    - KhÃ³a ngoáº¡i (Foreign Key)
    - Unique constraint
    - Optimistic locking Ä‘á»ƒ trÃ¡nh lost update

### 3.2 Nháº­p kho (Inbound)

- TÄƒng sá»‘ lÆ°á»£ng tá»“n kho tÆ°Æ¡ng á»©ng
- Ghi nháº­n má»™t báº£n ghi Stock Movement loáº¡i `INBOUND`
- Thá»±c thi toÃ n bá»™ logic trong má»™t transaction:
    - Cáº­p nháº­t tá»“n kho
    - LÆ°u log biáº¿n Ä‘á»™ng
    - Rollback toÃ n bá»™ náº¿u cÃ³ lá»—i xáº£y ra trong quÃ¡ trÃ¬nh xá»­ lÃ½

### 3.3 Xuáº¥t kho (Outbound)

- Giáº£m sá»‘ lÆ°á»£ng tá»“n kho
- Kiá»ƒm tra Ä‘á»§ sá»‘ lÆ°á»£ng trÆ°á»›c khi xuáº¥t
- KhÃ´ng cho phÃ©p xuáº¥t náº¿u tá»“n kho khÃ´ng Ä‘á»§
- Ghi nháº­n má»™t báº£n ghi Stock Movement loáº¡i `OUTBOUND`
- Äáº£m báº£o an toÃ n khi cÃ³ nhiá»u request Ä‘á»“ng thá»i:
    - Sá»­ dá»¥ng optimistic locking Ä‘á»ƒ trÃ¡nh lost update
    - Äáº·t ranh giá»›i transaction táº¡i service layer
    - Náº¿u cÃ³ xung Ä‘á»™t version: transaction bá»‹ rollback, client cÃ³ thá»ƒ retry
- Xá»­ lÃ½ lá»—i: náº¿u báº¥t ká»³ bÆ°á»›c nÃ o tháº¥t báº¡i â†’ toÃ n bá»™ transaction sáº½ rollback

### 3.4 XÃ¡c thá»±c & PhÃ¢n quyá»n

**XÃ¡c thá»±c:**

- Sá»­ dá»¥ng JWT (JSON Web Token)
- Token cÃ³ thá»i gian háº¿t háº¡n
- Máº­t kháº©u Ä‘Æ°á»£c mÃ£ hÃ³a báº±ng BCrypt

**PhÃ¢n quyá»n (RBAC):**

Há»‡ thá»‘ng há»— trá»£ 3 vai trÃ² chÃ­nh:

| Vai trÃ² | Quyá»n háº¡n |
|---------|-----------|
| **Admin** | Quáº£n lÃ½ sáº£n pháº©m, quáº£n lÃ½ kho, thá»±c hiá»‡n nháº­p/xuáº¥t kho, quáº£n lÃ½ ngÆ°á»i dÃ¹ng |
| **Staff** | Thá»±c hiá»‡n nháº­p/xuáº¥t kho, xem tá»“n kho |
| **Viewer** | Chá»‰ xem thÃ´ng tin tá»“n kho |

PhÃ¢n quyá»n Ä‘Æ°á»£c kiá»ƒm tra táº¡i service hoáº·c security layer trÆ°á»›c khi xá»­ lÃ½ nghiá»‡p vá»¥.

---

## 4. CÃ´ng nghá»‡ sá»­ dá»¥ng

| CÃ´ng nghá»‡ | Má»¥c Ä‘Ã­ch |
|-----------|----------|
| Java / Spring Boot | Framework chÃ­nh |
| JPA / Hibernate | ORM |
| PostgreSQL / MySQL | CÆ¡ sá»Ÿ dá»¯ liá»‡u |
| JWT | XÃ¡c thá»±c |
| Maven | Build tool |
| Docker | Container hÃ³a |
| Redis | Cache / Distributed locking |

---

## 5. Kiáº¿n trÃºc há»‡ thá»‘ng

Há»‡ thá»‘ng Ä‘Æ°á»£c thiáº¿t káº¿ theo mÃ´ hÃ¬nh **Modular Monolith**:

- Triá»ƒn khai dÆ°á»›i dáº¡ng má»™t á»©ng dá»¥ng duy nháº¥t
- Chia module theo domain: `auth`, `inventory`, `inbound`, `outbound`
- PhÃ¢n tÃ¡ch rÃµ rÃ ng service layer
- Sá»­ dá»¥ng rÃ ng buá»™c database Ä‘á»ƒ Ä‘áº£m báº£o tÃ­nh toÃ n váº¹n dá»¯ liá»‡u

> Chi tiáº¿t hÆ¡n Ä‘Æ°á»£c mÃ´ táº£ trong file `ARCHITECTURE.md`.

---

## 6. Thiáº¿t káº¿ cÆ¡ sá»Ÿ dá»¯ liá»‡u

Há»‡ thá»‘ng Ä‘Æ°á»£c thiáº¿t káº¿ theo hÆ°á»›ng Ä‘áº£m báº£o tÃ­nh toÃ n váº¹n dá»¯ liá»‡u, kháº£ nÄƒng má»Ÿ rá»™ng vÃ  an toÃ n khi xá»­ lÃ½ Ä‘á»“ng thá»i.

### 6.1 MÃ´ hÃ¬nh quáº£n lÃ½ tá»“n kho

Tá»“n kho Ä‘Æ°á»£c quáº£n lÃ½ theo tá»«ng cáº·p `(product, warehouse)`. Má»—i báº£n ghi tá»“n kho Ä‘áº¡i diá»‡n cho sá»‘ lÆ°á»£ng hiá»‡n táº¡i cá»§a má»™t sáº£n pháº©m táº¡i má»™t kho cá»¥ thá»ƒ.

**RÃ ng buá»™c:**
- Unique constraint trÃªn `(product_id, warehouse_id)`
- KhÃ´ng cho phÃ©p tá»“n kho Ã¢m á»Ÿ táº§ng nghiá»‡p vá»¥

**Lá»£i Ã­ch:**
- Dá»… má»Ÿ rá»™ng sang nhiá»u kho
- TrÃ¡nh trÃ¹ng láº·p dá»¯ liá»‡u tá»“n kho
- Äáº£m báº£o tÃ­nh nháº¥t quÃ¡n giá»¯a sáº£n pháº©m vÃ  vá»‹ trÃ­ lÆ°u trá»¯

### 6.2 Stock Movement Log

Má»i thay Ä‘á»•i vá» tá»“n kho Ä‘á»u Ä‘Æ°á»£c ghi láº¡i trong báº£ng `stock_movement`.

**Báº£ng nÃ y lÆ°u:**
- Loáº¡i biáº¿n Ä‘á»™ng (`INBOUND`, `OUTBOUND`)
- Sá»‘ lÆ°á»£ng thay Ä‘á»•i
- Thá»i Ä‘iá»ƒm thá»±c hiá»‡n
- NgÆ°á»i thá»±c hiá»‡n

**Lá»£i Ã­ch:**
- Truy váº¿t lá»‹ch sá»­ thay Ä‘á»•i
- Phá»¥c vá»¥ kiá»ƒm toÃ¡n (audit)
- CÃ³ thá»ƒ tÃ¡i táº¡o láº¡i tráº¡ng thÃ¡i tá»“n kho náº¿u cáº§n

### 6.3 RÃ ng buá»™c dá»¯ liá»‡u (Data Integrity)

Há»‡ thá»‘ng sá»­ dá»¥ng cÃ¡c cÆ¡ cháº¿ sau Ä‘á»ƒ Ä‘áº£m báº£o toÃ n váº¹n dá»¯ liá»‡u:

- Foreign key giá»¯a cÃ¡c báº£ng liÃªn quan
- Unique constraint Ä‘á»ƒ trÃ¡nh trÃ¹ng dá»¯ liá»‡u
- Validation á»Ÿ táº§ng service trÆ°á»›c khi ghi dá»¯ liá»‡u
- Transaction Ä‘áº£m báº£o tÃ­nh atomic

Viá»‡c káº¿t há»£p kiá»ƒm tra á»Ÿ cáº£ táº§ng á»©ng dá»¥ng vÃ  cÆ¡ sá»Ÿ dá»¯ liá»‡u giÃºp giáº£m rá»§i ro sai lá»‡ch dá»¯ liá»‡u.

### 6.4 Chiáº¿n lÆ°á»£c xá»­ lÃ½ Ä‘á»“ng thá»i (Concurrency Control)

Äá»ƒ trÃ¡nh hiá»‡n tÆ°á»£ng **lost update** khi nhiá»u request cÃ¹ng cáº­p nháº­t tá»“n kho:

- Sá»­ dá»¥ng **optimistic locking** (version column)
- Má»—i láº§n cáº­p nháº­t tá»“n kho sáº½ kiá»ƒm tra version hiá»‡n táº¡i
- Náº¿u cÃ³ xung Ä‘á»™t, transaction sáº½ rollback

CÃ¡ch tiáº¿p cáº­n nÃ y phÃ¹ há»£p vá»›i há»‡ thá»‘ng cÃ³ táº§n suáº¥t ghi trung bÃ¬nh vÃ  giÃºp:
- Giá»¯ hiá»‡u nÄƒng tá»‘t hÆ¡n so vá»›i locking cá»©ng
- Äáº£m báº£o dá»¯ liá»‡u khÃ´ng bá»‹ ghi Ä‘Ã¨ ngoÃ i Ã½ muá»‘n

> ERD Ä‘Æ°á»£c Ä‘áº·t táº¡i: `/docs/erd.png`

---

## 7. Chiáº¿n lÆ°á»£c xá»­ lÃ½ Concurrency

NgoÃ i optimistic locking, há»‡ thá»‘ng cÅ©ng cÃ³ thá»ƒ sá»­ dá»¥ng:

- **Pessimistic locking** (`SELECT FOR UPDATE`)
- **Atomic database update** Ä‘á»ƒ trÃ¡nh race condition
- **Distributed locking** (Redis) trong mÃ´i trÆ°á»ng scale-out
- **Queue-based processing** thÃ´ng qua message broker (Kafka/RabbitMQ)
- **Inventory reservation pattern** cho cÃ¡c nghiá»‡p vá»¥ cáº§n Ä‘áº£m báº£o tÃ­nh nháº¥t quÃ¡n cao

> Sequence diagram Ä‘Æ°á»£c Ä‘áº·t trong thÆ° má»¥c `/docs`.

---



Tài liệu secrets chi tiết: [docs/security-secrets.md](docs/security-secrets.md)

## 9. Cài đặt & chạy dự án

```bash
# 1. Clone repository
git clone https://github.com/<your-org>/<your-repo>.git

# 2. Vào thư mục dự án
cd wms

# 3. Cấu hình Environment Variables cho môi trường dev
# JWT_SECRET, JWT_EXPIRATION, DB_USERNAME, DB_PASSWORD,
# GOOGLE_GENAI_API_KEY, GOOGLE_GENAI_PROJECT_ID

# 4. Chạy ứng dụng với profile dev
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```
Hoặc nếu đã cài Maven global:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```
Ứng dụng chạy tại: [http://localhost:8080](http://localhost:8080)

## 10. Trá»ng tÃ¢m há»c táº­p

Dá»± Ã¡n táº­p trung vÃ o cÃ¡c váº¥n Ä‘á» backend cá»‘t lÃµi:

- Isolation level vÃ  transaction
- Data consistency
- Concurrency control
- Thiáº¿t káº¿ service layer rÃµ rÃ ng
- TÆ° duy má»Ÿ rá»™ng há»‡ thá»‘ng

---

## 11. Nháº­t kÃ½ nÄƒng cáº¥p há»‡ thá»‘ng (Cháº·ng Ä‘Æ°á»ng WMS Doanh Nghiá»‡p)

Dá»± Ã¡n Ä‘Ã£ tráº£i qua 7 Phase Ä‘á»ƒ tá»«ng bÆ°á»›c giáº£i quyáº¿t cÃ¡c bÃ i toÃ¡n hÃ³c bÃºa nháº¥t cá»§a Production:
1. **Core Entities**: Inbound, Outbound, Inventory + Stock Movement Log (Audit Trail) hoÃ n chá»‰nh.
2. **Concurrency Má»©c Ä‘á»™ CSDL**: Ãp dá»¥ng Optimistic Locking (`@Version`) Ä‘á»ƒ khÃ³a cá»©ng hiá»‡n tÆ°á»£ng Lost Update.
3. **Data Security**: XÃ¢y dá»±ng mÃ ng lÆ°á»›i Security RBAC (PhÃ¢n quyá»n JWT). 
4. **Performance**: Thiáº¿t láº­p Redis Cache xua tan ná»—i lo cho cÃ¡c báº£ng Read-Heavy (Product/Warehouse).
5. **Base Code Standards**: TÃ­ch há»£p Global Exception Handler bÃ³c tÃ¡ch lá»—i DTO Validation sang chuáº©n Response.
6. **Data Integrity (No Hard Delete)**: PhÃ¡ lá»‡nh xÃ³a cá»©ng. RÃ o cháº¯n SQL báº±ng cÆ¡ cháº¿ Soft Delete.
7. **Scale to Enterprise Level (Má»›i nháº¥t)**:
   - **Luá»“ng áº¢o (Java 21 Virtual Threads)**: QuÃ©t sáº¡ch Tomcat Thread truyá»n thá»‘ng, há»— trá»£ chá»‹u táº£i lÃªn tá»›i 10,000+ Concurrent Requests (chuáº©n thiáº¿t káº¿ á»©ng dá»¥ng System Design) vá»›i RAM tiáº¿t kiá»‡m nháº¥t.
   - **DB Connection Pool Manager**: Cá»©u há»™ DB khÃ´ng thá»Ÿ gáº¥p báº±ng hÃ ng rÃ o Hikari (Max-Pool-Size).
   - **Spring Data Pagination**: Báº£o vá»‡ tÃ i nguyÃªn, cáº¥m tiá»‡t truy váº¥n `List<T> findAll()`, chuáº©n hÃ³a xuáº¥t hÃ ng loáº¡t báº±ng `Page<T>` Ä‘á»ƒ chá»‘ng Memory Leak.

---

## 12. HÆ°á»›ng phÃ¡t triá»ƒn trong tÆ°Æ¡ng lai

- [ ] PhÃ¢n tÃ¡n hÃ³a Module (Microservices DB per Service)
- [ ] ThÃªm idempotency cho tÃ¡c vá»¥ Outbound
- [ ] Ghi log báº¥t Ä‘á»“ng bá»™ báº±ng Kafka hoáº·c RabbitMQ
- [ ] TrÃ¬nh quáº£n lÃ½ sÆ¡ Ä‘á»“ Database Version Control (Sá»­ dá»¥ng Flyway)



