# AIMS Shop — Quick Start Guide

## Prerequisites
| Tool | Version | Notes |
|------|---------|-------|
| Docker Desktop | 4.x+ | For PostgreSQL + Redis |
| Java JDK | 17+ | For Spring Boot backend |
| Maven | 3.9+ | IntelliJ bundled or system |
| Node.js | 18+ | For Angular frontend |
| npm | 9+ | Comes with Node.js |

---

## Step 1 — Start Infrastructure (Docker)
```powershell
# From AIMS shop NEW folder:
docker-compose up -d
```
This starts:
- **PostgreSQL 15** on port `5435` (DB: `aims_db`, user: `aims_user`, pass: `aims_password`)
- **Redis 7** on port `6379`

Wait ~10 seconds for DB to be healthy.

---

## Step 2 — Start Backend (Spring Boot)
```powershell
# From aims-backend folder:
cd aims-backend

# Using IntelliJ bundled Maven:
& "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2024.3.3\plugins\maven\lib\maven3\bin\mvn.cmd" spring-boot:run

# OR open in IntelliJ and run AimsBackendApplication.java directly
```

The backend starts on **http://localhost:8080**

On first run, `DataInitializer` automatically seeds:
- **5 Books** (To Kill a Mockingbird, Great Gatsby, 1984, The Alchemist, Don Quixote)
- **3 CDs** (Thriller, Millennium, Back in Black)
- **3 DVDs** (Inception, Interstellar, The Godfather)
- **3 Newspapers** (Tuổi Trẻ, Thanh Niên, The Economist)
- **Admin account**: `admin` / `admin123`
- **Manager account**: `manager` / `manager123`

---

## Step 3 — Start Frontend (Angular 17)
```powershell
# From aims-frontend folder:
cd aims-frontend
npm install        # first time only
npm start          # → http://localhost:4200
```

---

## Application URLs
| Service | URL | Notes |
|---------|-----|-------|
| Frontend | http://localhost:4200 | Customer-facing shop |
| Backend API | http://localhost:8080/api | REST API |
| Admin Login | http://localhost:4200/login | `admin` / `admin123` |
| Manager Login | http://localhost:4200/login | `manager` / `manager123` |

---

## Feature Map

### Customer Features
| Feature | Route | AIMS Spec |
|---------|-------|-----------|
| Home / Browse | `/home` | UC001 — View home page |
| Search Products | `/search` | UC008 — Search products |
| Product Detail | `/product/:id` | UC002 — View product details |
| Cart | `/cart` | UC003 — Manage cart |
| Checkout | `/checkout` | UC006-007 — Place order |
| Payment | `/payment` | UC007 — VietQR / PayPal mock |
| Track Orders | `/order` | UC011 — View orders by email |
| Order Detail | `/order/:id` | UC011 — View order details + cancel |

### Manager Features (login: manager/manager123)
| Feature | Route | AIMS Spec |
|---------|-------|-----------|
| Dashboard | `/manager/dashboard` | Overview + KPIs |
| Product List | `/manager/products` | UC004 — List products |
| Add Product | `/manager/product-form` | UC004 — Add product |
| Edit Product | `/manager/product-form/:id` | UC005 — Edit product |
| Order Processing | `/manager/orders` | UC009/010 — Approve/Reject |
| Stock History | `/manager/stock-history` | Manual adjustments |

### Admin Features (login: admin/admin123)
| Feature | Route | AIMS Spec |
|---------|-------|-----------|
| Admin Dashboard | `/admin/dashboard` | System overview |
| User Management | `/admin/users` | UC012-015 — CRUD staff |

---

## Architecture

```
aims-frontend/          ← Angular 17 (Standalone Components, Signals)
├── core/
│   ├── models/         ← TypeScript interfaces (Media, Order, User)
│   ├── services/       ← ApiService, AuthService, CartService
│   ├── interceptors/   ← JWT auth + error interceptors
│   └── guards/         ← managerGuard, adminGuard
├── shared/             ← Navbar, Footer, ProductCard (BEM SCSS)
└── features/
    ├── auth/login/
    ├── customer/       ← home, search, product-detail, cart, checkout, payment, orders
    ├── manager/        ← dashboard, product-management, product-form, order-processing, stock-history
    └── admin/          ← dashboard, user-management

aims-backend/           ← Spring Boot 3.2.5 + JPA + Security
├── entity/             ← Media (abstract), Book, CD, DVD, Newspaper, Order, User, ...
├── repository/         ← Spring Data JPA
├── service/            ← Interfaces + Implementations
├── controller/         ← REST API (Auth, Media, Order, Shipping, Stock, User)
├── security/           ← JWT filter + UserDetailsService
├── strategy/           ← Shipping (Standard + Rush)
├── adapter/            ← PayPal + VietQR (mock gateways)
└── config/             ← SecurityConfig + DataInitializer
```

## OOP Design Patterns
| Pattern | Location | Purpose |
|---------|----------|---------|
| **Inheritance** | `Media ← Book/CD/DVD/Newspaper` | Polymorphic media types |
| **Strategy** | `StandardShippingStrategy`, `RushShippingStrategy` | Configurable shipping calc |
| **Adapter** | `PayPalAdapter`, `VietQRAdapter` | Unified payment gateway interface |
| **Factory** | `PaymentGatewayFactory` | Select gateway by payment method |
| **Template Method** | Entity domain methods (`approve`, `reject`, `block`) | Rich domain model |

## Mock Services
- **VietQR**: Generates a fake QR display — no real bank connection
- **PayPal**: Returns a fake transaction ID — no real payment
- **Email**: All emails are logged to console via `[MOCK EMAIL]` prefix
