# AIMS — Danh sách thay đổi

---

## I. Sửa Logic Nghiệp Vụ Chưa Hoàn Thiện

### 1. VietQrPaymentHandler — `initiate` trả về chuỗi định danh giao dịch
**File:** `backend/src/main/java/com/aims/payment/VietQrPaymentHandler.java`

**Trước:** `initiate(int totalAmount)` luôn trả `""`, khiến đơn hàng không có `paymentTransactionId`.

**Sau:** Sinh UUID có tiền tố `VQR-` làm mã tham chiếu giao dịch (`VQR-XXXXXXXXXX`). Mã này được lưu vào `Order.paymentTransactionId` để theo dõi nội bộ. QR code vẫn được tạo qua `GET /api/vqr/generate-qr` khi frontend hiển thị.

**Lý do không thêm `Refundable`:** Theo spec AIMS, hoàn tiền VietQR là thủ công — quản lý chuyển khoản lại cho khách, hệ thống chỉ gửi email thông báo. Đây là đúng business rule.

---

### 2. PaymentService — Chỉ hoàn tiền khi đơn hàng đã thanh toán
**File:** `backend/src/main/java/com/aims/service/PaymentService.java`

**Trước:** `processRefund` gọi handler và gửi email cho manager kể cả khi đơn chưa được thanh toán (`paymentStatus = PENDING`).

**Sau:** Kiểm tra `paymentStatus == PAID` trước; nếu chưa thanh toán, trả về `true` ngay (không cần hoàn tiền).

---

### 3. NewsletterController — Lưu email người đăng ký vào database
**Files mới:**
- `backend/.../entity/NewsletterSubscriber.java` — Entity với trường `email` (unique) và `subscribedAt`.
- `backend/.../repository/NewsletterSubscriberRepository.java` — JPA repository với `existsByEmail`.
- `backend/.../controller/NewsletterController.java` — Cập nhật: kiểm tra trùng lặp rồi lưu subscriber trước khi gửi email xác nhận.

**Trước:** Chỉ gọi `sendNewsletterConfirmation()`, không lưu dữ liệu nào.

**Sau:** Lưu email vào bảng `newsletter_subscribers` (dedup by email), sau đó gửi xác nhận.

---

### 4. ContactController — Lưu phản hồi liên hệ vào database
**Files mới:**
- `backend/.../entity/ContactSubmission.java` — Entity với `name`, `email`, `subject`, `message`, `submittedAt`.
- `backend/.../repository/ContactSubmissionRepository.java` — JPA repository.
- `backend/.../controller/ContactController.java` — Cập nhật: lưu submission vào bảng `contact_submissions` rồi gửi email cho manager.

**Trước:** Chỉ gửi email, không lưu dữ liệu.

**Sau:** Lưu vào DB để quản lý và tra cứu lại được.

---

## II. Bảo Mật & Cấu Hình

### 5. SecurityConfig — CORS đọc từ biến môi trường
**File:** `backend/.../config/SecurityConfig.java`

**Trước:** `config.setAllowedOrigins(List.of("http://localhost:4200"))` — hardcode.

**Sau:** Inject `@Value("${app.frontend.url:http://localhost:4200}")` và dùng `frontendUrl` trong CORS. Khi deploy production, set env var `FRONTEND_URL=https://your-app.vercel.app`.

---

### 6. application.yml — Toàn bộ secrets đọc từ biến môi trường
**File:** `backend/src/main/resources/application.yml`

Tất cả giá trị nhạy cảm giờ theo dạng `${ENV_VAR:default_value}`:

| Biến môi trường | Mục đích |
|---|---|
| `DATABASE_URL` | JDBC URL PostgreSQL |
| `DB_USERNAME` | User DB |
| `DB_PASSWORD` | Password DB |
| `JWT_SECRET` | Khóa ký JWT |
| `FRONTEND_URL` | Origin cho CORS |
| `MANAGER_EMAIL` | Email nhận thông báo |
| `ADMIN_PASSWORD` | Mật khẩu seed admin (chỉ chạy lần đầu) |
| `MANAGER_PASSWORD` | Mật khẩu seed manager (chỉ chạy lần đầu) |
| `MAIL_HOST/USERNAME/PASSWORD` | SMTP config |
| `PAYPAL_CLIENT_ID/SECRET` | PayPal API |
| `CLOUDINARY_*` | Cloudinary API |
| `PORT` | Port server (Railway tự inject) |

Đã xóa cấu hình Redis vì không có bất kỳ component Java nào sử dụng Redis.

---

### 7. DataInitializer — Mật khẩu seed từ env var
**File:** `backend/.../config/DataInitializer.java`

**Trước:** `passwordEncoder.encode("admin123")` và `"manager123"` hardcode.

**Sau:** Inject từ `${app.admin.password}` và `${app.manager-account.password}` (đọc từ `ADMIN_PASSWORD` / `MANAGER_PASSWORD` env var).

---

## III. Dead Code Đã Xóa

### 8. Xóa `api.service.ts`
**File:** `frontend/src/app/core/services/api.service.ts`

Nội dung chỉ là `export {};` — không có component nào import, không có logic nào. Đã xóa.

### 9. Xóa `contact-api.service.ts`
**File:** `frontend/src/app/core/services/contact-api.service.ts`

Không có component nào import `ContactApiService`. Logic `submitContact` và `subscribeNewsletter` đã tồn tại trong `MediaApiService` và được tất cả components dùng trực tiếp từ đó. Đã xóa.

### 10. Xóa Redis khỏi pom.xml
**File:** `backend/pom.xml`

Xóa `spring-boot-starter-data-redis` — không có class Java nào dùng Redis template hay `@Cacheable`. Xóa tránh lỗi kết nối Redis khi deploy lên cloud không có Redis.

---

## IV. Frontend — Environment Config

### 11. Tạo environment files
**Files mới:**
- `frontend/src/environments/environment.ts` — Dev: `apiUrl: 'http://localhost:8080/api'`
- `frontend/src/environments/environment.prod.ts` — Prod: `apiUrl: '/api'` (dùng Vercel proxy)

### 12. Cập nhật angular.json — fileReplacements production
**File:** `frontend/angular.json`

Thêm `fileReplacements` trong configuration `production` để Angular tự swap `environment.ts` → `environment.prod.ts` khi build production.

### 13. Cập nhật tất cả API services — xóa hardcode localhost
Tất cả 6 services đã được cập nhật để import `environment` và dùng `environment.apiUrl`:
- `auth.service.ts`
- `media-api.service.ts`
- `order-api.service.ts`
- `user-api.service.ts`
- `stock-api.service.ts`
- `file-upload.service.ts`

---

## V. Deploy Infrastructure

### 14. docker-compose.yml — dùng biến môi trường
**File:** `docker-compose.yml`

Xóa Redis service (không dùng). Credentials PostgreSQL đọc từ `.env` với fallback mặc định (`${POSTGRES_USER:-aims_user}`).

### 15. Tạo `.env.example`
**File:** `.env.example`

Template đầy đủ tất cả biến môi trường cần thiết. Copy thành `.env` để chạy local.

### 16. Tạo `frontend/vercel.json`
**File:** `frontend/vercel.json`

Config cho Vercel:
- Rewrite `/api/*` → proxy tới backend trên Railway/Render (cập nhật URL backend thực tế).
- Rewrite `/*` → `index.html` (SPA routing).
- Thêm security headers.

### 17. Tạo `backend/Dockerfile`
**File:** `backend/Dockerfile`

Multi-stage build: Maven build stage + JRE runtime stage. Dùng cho Railway/Render/Fly.io.

---

## VI. Hướng Dẫn Deploy

### Backend (Railway / Render)
1. Tạo project mới trên Railway hoặc Render.
2. Connect repo, chọn thư mục `backend/`.
3. Railway/Render tự detect Dockerfile.
4. Set các env vars (copy từ `.env.example`), đặc biệt:
   - `DATABASE_URL` — lấy từ Railway PostgreSQL hoặc Neon
   - `FRONTEND_URL` — URL Vercel của frontend (vd: `https://aims-shop.vercel.app`)
   - `JWT_SECRET` — thay bằng chuỗi random mạnh
   - Các env var email và PayPal

### Database (Neon — free persistent PostgreSQL)
1. Tạo tài khoản tại [neon.tech](https://neon.tech).
2. Tạo database, copy connection string dạng:
   `postgresql://user:pass@ep-xxx.neon.tech/neondb?sslmode=require`
3. Chuyển thành JDBC format:
   `jdbc:postgresql://ep-xxx.neon.tech/neondb?sslmode=require`
4. Set `DATABASE_URL` trên Railway/Render bằng JDBC URL trên.

### Frontend (Vercel)
1. Tạo project mới trên Vercel, connect repo.
2. **Root directory:** `frontend`
3. **Build command:** `ng build --configuration production`
4. **Output directory:** `dist/frontend/browser`
5. Cập nhật `destination` trong `frontend/vercel.json` thành URL backend thực tế.
6. Set env var `FRONTEND_URL` trên backend = URL Vercel của frontend (để CORS đúng).

---

## VII. Ghi Chú Kỹ Thuật

**VietQR Refund Flow (spec-compliant):**
- Khách hàng thanh toán qua QR → `paymentStatus = PENDING` (chờ xác nhận thủ công)
- Manager approve đơn hàng sau khi xác minh ngân hàng đã nhận tiền
- Nếu reject/cancel đơn đã thanh toán (`paymentStatus = PAID`): hệ thống gửi email cho manager để chuyển khoản hoàn tiền thủ công
- `processRefund` trả `false` cho VietQR = đúng, nghĩa là "hệ thống không tự động hoàn tiền được"
- `sendOrderCancelled(..., refundIssued=false)` thông báo cho khách biết hoàn tiền chờ xử lý thủ công

**Lưu ý `ddl-auto: update`:** Đủ cho demo và môi trường staging. Với production thực tế nên dùng Flyway/Liquibase để migration có kiểm soát.
