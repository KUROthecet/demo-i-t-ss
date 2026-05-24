<div align="center">
  <br />
  <h1>🛒 AIMS Shop</h1>
  <p>
    <strong>A Modern, High-Performance E-Commerce Platform</strong>
  </p>
  <p>
    <img src="https://img.shields.io/badge/Angular-17-DD0031?style=for-the-badge&logo=angular" alt="Angular" />
    <img src="https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?style=for-the-badge&logo=spring-boot" alt="Spring Boot" />
    <img src="https://img.shields.io/badge/PostgreSQL-15-336791?style=for-the-badge&logo=postgresql" alt="PostgreSQL" />
    <img src="https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker" alt="Docker" />
  </p>
</div>

<br />

## 📖 About The Project

**AIMS Shop** (A Store Management System) is an enterprise-grade e-commerce application designed with a robust **micro-architecture** approach. It bridges a lightning-fast, reactive frontend with a secure, highly-scalable backend. 

Engineered with **SOLID** principles and classic **Gang of Four (GoF) Design Patterns**, AIMS Shop guarantees maintainability, testability, and enterprise-level reliability.

### ✨ Key Highlights
- **Stunning UI/UX:** Built with modern CSS architectures (BEM), fluid typography, and glassmorphism components.
- **Lightning Fast Search:** Server-side pagination mapping natively to dynamic Angular 17 views.
- **Enterprise Security:** JWT-based authentication with Role-Based Access Control (Admin, Manager, Customer).
- **Pattern-Driven Backend:** Extensive use of Strategy, Factory, and Adapter patterns to handle Payments and Shipping.

---

## 🛠 Built With

### Frontend (Client-Side)
- **Framework:** Angular 17 (Standalone Components, Signals)
- **Styling:** Vanilla SCSS (BEM Methodology, CSS Variables, Responsive Design)
- **Animation:** GSAP (GreenSock) for high-performance scroll triggers and micro-interactions
- **Tooling:** Node.js v18+, NPM v9+

### Backend (Server-Side)
- **Framework:** Spring Boot 3.2.5
- **Language:** Java 17
- **Persistence:** Spring Data JPA / Hibernate
- **Security:** Spring Security (JWT Tokens)
- **Build Tool:** Maven

### Infrastructure & DevOps
- **Database:** PostgreSQL 15 (Dockerized)
- **Caching:** Redis 7 (Dockerized)
- **Containerization:** Docker & Docker Compose

---

## 🚀 Getting Started

Follow these steps to get a local copy up and running in less than a minute.

### 1. Prerequisites

Before you begin, ensure you have the following installed:
* **Docker Desktop** (v4.x+)
* **Java JDK** (17+)
* **Node.js** (v18+) & **NPM** (v9+)

### 2. One-Click Fast Startup (Recommended)

To make launching the project as effortless as possible, we have provided an automated startup script.

```cmd
# Navigate to the project root directory and run:
start_aims.bat
```
*(Alternatively, you can just double-click `start_aims.bat` in Windows File Explorer).*

**What this script does automatically:**
1. Spins up the **PostgreSQL** and **Redis** Docker containers via `docker-compose`.
2. Opens a new terminal and boots up the **Spring Boot Backend** (`mvnw spring-boot:run`). The backend will automatically seed the database on its first run with over 14,000+ realistic product records.
3. Opens a new terminal, installs Node dependencies (`npm install`), and boots up the **Angular Frontend** (`npm start`).

### 3. Application URLs

Once the script completes, the services will be available at:
* **Frontend:** `http://localhost:4200`
* **Backend API:** `http://localhost:8080/api`
* **Database:** `localhost:5435`

---

## 🔐 Default Credentials

Upon first initialization, the database is seeded with the following roles:

| Role | Username | Password | Access Level |
|------|----------|----------|--------------|
| **Admin** | `admin` | `admin123` | User Management, System Overview |
| **Manager** | `manager` | `manager123` | Product Catalog, Order Processing, Stock History |
| **Customer** | *Self-Register* | *Custom* | Browsing, Cart, Checkout, Order Tracking |

---

## 📐 Architecture & Design Patterns

AIMS Shop is heavily influenced by clean architecture and object-oriented design principles. 

### Core Design Patterns Implemented
- **Strategy Pattern (`RushShippingStrategy`, `StandardShippingStrategy`):** Encapsulates diverse shipping calculation algorithms, allowing the system to switch shipping logic at runtime without modifying the context.
- **Adapter Pattern (`PayPalAdapter`, `VietQRAdapter`):** Normalizes disparate external payment gateways into a single, unified `PaymentGateway` interface.
- **Factory Method (`PaymentGatewayFactory`):** Centralizes the creation logic for payment processors, decoupling the checkout flow from specific payment implementations.
- **Inheritance & Polymorphism:** The abstract `Media` entity acts as a base class for `Book`, `CD`, `DVD`, and `Newspaper`, allowing the repository and service layers to process all items polymorphically.

### Detailed Directory Structure

```text
.
├── backend/                  # Spring Boot 3 API
│   ├── src/main/java/com/aims/
│   │   ├── adapter/          # Payment Gateways implementation (Adapter Pattern)
│   │   ├── config/           # Security, CORS, and Data Seeding (DataInitializer)
│   │   ├── controller/       # REST API Endpoints (Auth, Media, Order, Shipping, User)
│   │   ├── dto/              # Data Transfer Objects (request/ and response/)
│   │   ├── entity/           # JPA Domain Models (Media, Book, CD, Order, User...)
│   │   ├── enums/            # Enums (MediaStatus, OrderStatus, PaymentMethod...)
│   │   ├── exception/        # Global Exception Handler and custom exceptions
│   │   ├── repository/       # Spring Data JPA interfaces for database queries
│   │   ├── security/         # JWT Token Provider, JWT Filter, UserDetailsService
│   │   ├── service/          # Business logic interfaces and implementations
│   │   └── strategy/         # Shipping Logic (Standard vs Rush - Strategy Pattern)
│   └── src/main/resources/   # Application properties (application.yml) & SQL scripts
│
├── frontend/                 # Angular 17 Client
│   ├── src/app/
│   │   ├── core/             # Singletons: Models, Services (API, Auth, Cart), Guards, Interceptors
│   │   ├── features/         # Feature Modules:
│   │   │   ├── admin/        # Admin panel (Dashboard, User Management)
│   │   │   ├── customer/     # Customer storefront (Home, Search, Cart, Checkout, Orders...)
│   │   │   └── manager/      # Product Manager panel (Dashboard, Product Form, Stock History)
│   │   └── shared/           # Reusable UI Components (Navbar, Footer, Product Cards) & Pipes
│
├── start_aims.bat            # One-click startup script (Automates DB, Backend, and Frontend)
└── docker-compose.yml        # Infrastructure Definition (PostgreSQL, Redis)
```


