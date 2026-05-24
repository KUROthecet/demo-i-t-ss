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

Follow these steps to get a local copy up and running.

### 1. Prerequisites

Before you begin, ensure you have the following installed:
* **Docker Desktop** (v4.x+)
* **Java JDK** (17+)
* **Node.js** (v18+) & **NPM** (v9+)

### 2. Infrastructure Setup (Docker)

Spin up the required database and cache containers:

```bash
# From the root directory of the project
docker-compose up -d
```
*This exposes PostgreSQL on port `5435` and Redis on port `6379`.*

### 3. Backend Setup

The backend will automatically seed the database on its first run with over 14,000+ realistic product records and default admin/manager accounts.

```bash
# Navigate to the backend directory
cd backend

# Run the Spring Boot application (Windows)
.\mvnw.cmd spring-boot:run

# Or run via your preferred IDE (IntelliJ / Eclipse)
```
*The backend API will be available at `http://localhost:8080/api`*

### 4. Frontend Setup

```bash
# Navigate to the frontend directory
cd frontend

# Install dependencies
npm install

# Start the development server
npm start
```
*The frontend application will be available at `http://localhost:4200`*

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

### Directory Structure

```text
.
├── backend/                  # Spring Boot 3 API
│   ├── src/main/java/com/aims/
│   │   ├── adapter/          # Payment Gateways (Adapter Pattern)
│   │   ├── config/           # Security, CORS, and Data Seeding
│   │   ├── controller/       # REST API Endpoints
│   │   ├── dto/              # Data Transfer Objects
│   │   ├── entity/           # JPA Domain Models
│   │   ├── repository/       # Spring Data Repositories
│   │   ├── security/         # JWT Filters & Providers
│   │   ├── service/          # Business Logic Layer
│   │   └── strategy/         # Shipping Logic (Strategy Pattern)
│
├── frontend/                 # Angular 17 Client
│   ├── src/app/
│   │   ├── core/             # Singletons, Models, Interceptors, Guards
│   │   ├── features/         # Feature Modules (Customer, Manager, Admin)
│   │   └── shared/           # Reusable UI Components & Pipes
│
└── docker-compose.yml        # Infrastructure Definition
```

---

## 🤝 Contributing

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📝 License

Distributed under the MIT License. See `LICENSE` for more information.

<br />
<div align="center">
  <sub>Built with ❤️ by the AIMS Engineering Team.</sub>
</div>
