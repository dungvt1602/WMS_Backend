# Security Secrets Guide (WMS)

## 1) Mục tiêu
- Tách toàn bộ thông tin nhạy cảm ra khỏi source code.
- Dev local dùng Environment Variables.
- Staging/Production dùng Vault.

## 2) Mapping secret keys chuẩn
Các key dưới đây phải được quản lý bên ngoài code:
- `jwt.secret`
- `jwt.expiration`
- `spring.datasource.username`
- `spring.datasource.password`
- `spring.ai.google.genai.api-key`
- `spring.ai.google.genai.project-id`

Note quan trọng:
- Ưu tiên giữ nguyên tên key theo Spring property để app bind tự động.
- Không đổi key tùy tiện giữa dev/prod để tránh lỗi môi trường.

## 3) Môi trường DEV (không cần Vault)
DEV dùng biến môi trường (User Environment Variables hoặc `setx`).

Ứng với file:
- `src/main/resources/application-dev.properties`

Ví dụ biến môi trường cần có:
- `JWT_SECRET`
- `JWT_EXPIRATION`
- `DB_USERNAME`
- `DB_PASSWORD`
- `GOOGLE_GENAI_API_KEY`
- `GOOGLE_GENAI_PROJECT_ID`

Note quan trọng cho dev:
- Sau khi set env, phải mở terminal/IDE mới để nhận biến.
- Không commit file chứa secret thật (`.env`, txt, screenshot, export env).
- Không in secret ra log khi debug.

## 4) Môi trường PROD (dùng Vault)
Prod đọc secret từ Vault qua Spring Cloud Vault.

Ứng với file:
- `src/main/resources/application-prod.properties`

Các biến bootstrap bắt buộc trên server:
- `VAULT_URI`
- `VAULT_TOKEN` (hoặc cơ chế auth khác như AppRole)

Path secret chuẩn:
- `secret/wms` (KV v2)

Ví dụ seed dữ liệu vào Vault:
```bash
vault kv put secret/wms \
  jwt.secret="BASE64_32_BYTES_MIN" \
  jwt.expiration="86400000" \
  spring.datasource.username="wms_user" \
  spring.datasource.password="***" \
  spring.ai.google.genai.api-key="***" \
  spring.ai.google.genai.project-id="demo-project"
```

Note quan trọng cho prod:
- Token Vault chỉ cấp quyền `read` đúng path `secret/wms`.
- Không dùng 1 token chung cho nhiều service.
- Bật audit log của Vault để truy vết truy cập secret.

## 5) Rotation (đổi secret an toàn)
Áp dụng cho `jwt.secret`, DB password, API keys.

Quy trình đề xuất:
1. Tạo secret mới trong Vault.
2. Cập nhật value tại `secret/wms`.
3. Restart app (hoặc refresh config nếu có cơ chế reload).
4. Theo dõi log/auth và rollback nếu lỗi.

Note quan trọng:
- Đổi `jwt.secret` sẽ làm token cũ không còn hợp lệ (đúng theo bảo mật).
- Cần thông báo window bảo trì ngắn cho client nếu hệ thống đang active lớn.

## 6) Checklist trước khi deploy production
1. `application-prod.properties` đã có `spring.config.import=vault://`.
2. Server có `VAULT_URI` và auth hợp lệ (`VAULT_TOKEN`/AppRole).
3. Vault có đủ key theo mục 2.
4. Policy Vault đúng nguyên tắc least privilege.
5. App chạy `prod` profile thành công.
6. Đảm bảo không có secret hardcode trong repo.

## 7) Checklist review cho dev (trước khi merge)
1. Có thêm/đổi config nhạy cảm nào không?
2. Secret mới đã đưa ra env/Vault chưa?
3. Có log/debug nào vô tình in secret không?
4. Có cập nhật tài liệu mapping key ở file này chưa?

