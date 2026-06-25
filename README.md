# Food Court Microservices

A microservices-based food court management system built with **Spring Boot** and **Spring Cloud**. The platform covers user authentication, restaurant and menu management, order processing, and Razorpay payment integration — with service discovery, a centralized config server, and an API gateway as the single entry point.

---

## Project Overview

| Capability | Service |
|------------|---------|
| User registration, login, and JWT issuance | Auth Service |
| Restaurant and menu CRUD | Restaurant Service |
| Order placement, tracking, and delivery OTP | Order Service |
| Razorpay payment creation and verification | Payment Service |
| Request routing and JWT validation | API Gateway |
| Service registration and discovery | Eureka Server |
| Centralized configuration (Git-backed) | Config Server |

---

## Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                    Client (Web / Mobile)                     │
└──────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌──────────────────────────────────────────────────────────────┐
│              API Gateway  (Port 8080)                        │
│         JWT validation · Route to services via Eureka        │
└──────────────────────────────────────────────────────────────┘
         │              │              │              │
         ▼              ▼              ▼              ▼
   ┌──────────┐   ┌────────────┐   ┌──────────┐   ┌───────────┐
   │  Auth    │   │ Restaurant │   │  Order   │   │  Payment  │
   │ Service  │   │  Service   │   │ Service  │   │  Service  │
   │  (8081)  │   │   (8082)   │   │  (8083)  │   │  (8084)   │
   └──────────┘   └────────────┘   └──────────┘   └───────────┘
         │              │              │              │
         └──────────────┴──────────────┴──────────────┘
                              │
              ┌───────────────┴───────────────┐
              ▼                               ▼
   ┌─────────────────────┐         ┌─────────────────────┐
   │   Eureka Server     │         │   Config Server     │
   │   (Port 8761)       │         │   (Port 8888)       │
   └─────────────────────┘         └─────────────────────┘
                                              │
                                              ▼
                                   Git config repository
```

### Order & Payment Flow

```
Customer creates order (Order Service)
        │
        ├─ COD ──────────────────► Order status → CONFIRMED
        │
        └─ Online (UPI / CARD / NET_BANKING)
                │
                ▼
        Order Service ──Feign──► Payment Service
                │              (creates Razorpay order)
                ▼
        Client completes payment on Razorpay
                │
                ▼
        Payment Service verifies signature
                │
                ▼
        Payment Service ──Feign──► Order Service
        (X-Internal-Token)         (payment-success → CONFIRMED)
```

---

## Services

### 1. Eureka Server — Port `8761`
- Service registry for all microservices
- Does not register itself as a client

### 2. Config Server — Port `8888`
- Serves shared configuration from a **Git repository**
- Repository: `https://github.com/ankitgangwar1082006/foodcourt-config-repo.git`
- Requires `CONFIG_REPO_PAT` environment variable (GitHub personal access token)
- Services load config via `spring.config.import: optional:configserver:http://localhost:8888`

### 3. API Gateway — Port `8080`
- Single entry point for all client requests
- Routes traffic to services registered in Eureka
- JWT authentication filter on secured routes
- **Public routes** (no token required): `/api/users/register`, `/api/users/login`

### 4. Auth Service — Port `8081`
- User registration and login
- JWT token generation (24-hour expiry)
- BCrypt password hashing
- Roles: `CUSTOMER`, `RESTAURANT_OWNER`
- MySQL database: `users`, `user_profiles`

### 5. Restaurant Service — Port `8082`
- Restaurant CRUD (owner-scoped create/update/delete)
- Menu item CRUD (owner-scoped create/update/delete)
- Paginated listing for restaurants and menu items
- MySQL database: `restaurants`, `menu_items`

### 6. Order Service — Port `8083`
- Order creation with restaurant and menu item validation
- Supports **COD** and online payment methods (`UPI`, `CARD`, `NET_BANKING`)
- Paginated order history per user
- Restaurant owners can update order status
- Delivery OTP verification to mark orders as `DELIVERED`
- Communicates with Restaurant and Payment services via **OpenFeign**
- MySQL database: `orders`, `order_items`

### 7. Payment Service — Port `8084`
- Razorpay order creation and payment signature verification
- Persists payment records
- On successful verification, notifies Order Service via internal Feign call
- MySQL database: `payments`

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 4.1.0 (API Gateway: 3.2.5) |
| Language | Java 21 |
| Build Tool | Maven |
| Service Discovery | Spring Cloud Netflix Eureka |
| API Gateway | Spring Cloud Gateway |
| Configuration | Spring Cloud Config Server (Git backend) |
| Inter-service Communication | OpenFeign with fallback classes |
| Authentication | JWT (jjwt 0.11.5) |
| Security | Spring Security |
| Database | MySQL with Spring Data JPA |
| Payments | Razorpay Java SDK |
| Utilities | Lombok, Jakarta Validation |

---

## Prerequisites

- **Java 21**
  ```bash
  java -version
  ```
- **Maven 3.6+**
  ```bash
  mvn -version
  ```
- **MySQL** — separate database per service (configured via Config Server)
- **GitHub Personal Access Token** — for Config Server to pull the config repository
- **Razorpay account** — API key ID and secret (for Payment Service)

---

## Environment Variables

Set these before starting the Config Server and dependent services:

| Variable | Used By | Description |
|----------|---------|-------------|
| `CONFIG_REPO_PAT` | Config Server | GitHub PAT to access the config repository |
| `CONFIG_SERVER_URL` | All services | Config Server URL (default: `http://localhost:8888`) |

The following are managed in the **external config repository** (not in local `application.yml`):

| Property | Description |
|----------|-------------|
| `secret.key` | Base64-encoded JWT signing key (shared across Auth, Gateway, Order, Restaurant) |
| `secret.token` | Internal service-to-service authentication token |
| `razorpay.key.id` | Razorpay API key ID |
| `razorpay.key.secret` | Razorpay API key secret |
| `spring.datasource.*` | MySQL connection settings per service |
| `server.port` | Service ports |
| `spring.cloud.gateway.routes` | API Gateway route definitions |

---

## Quick Start

### 1. Clone and build

```bash
cd Food_Court_Microservices

# Build each service individually
cd eureka-server       && mvn clean install && cd ..
cd config_server       && mvn clean install && cd ..
cd auth-service        && mvn clean install && cd ..
cd restaurant-service  && mvn clean install && cd ..
cd order-service       && mvn clean install && cd ..
cd payment-service     && mvn clean install && cd ..
cd api-gateway         && mvn clean install && cd ..
```

### 2. Start services in order

> Start infrastructure first, then business services, then the gateway.

**Step 1 — Eureka Server**
```bash
cd eureka-server
mvn spring-boot:run
# Dashboard: http://localhost:8761
```

**Step 2 — Config Server**
```bash
cd config_server
set CONFIG_REPO_PAT=your_github_pat   # Windows
# export CONFIG_REPO_PAT=your_github_pat   # Linux/macOS
mvn spring-boot:run
```

**Step 3 — Business services** (one terminal per service)
```bash
cd auth-service        && mvn spring-boot:run
cd restaurant-service  && mvn spring-boot:run
cd order-service       && mvn spring-boot:run
cd payment-service     && mvn spring-boot:run
```

**Step 4 — API Gateway**
```bash
cd api-gateway
mvn spring-boot:run
# Entry point: http://localhost:8080
```

---

## API Endpoints

All client requests should go through the **API Gateway** at `http://localhost:8080` unless testing a service directly.

### Auth Service — `/api/users`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/users/register` | No | Register a new user |
| `POST` | `/api/users/login` | No | Login and receive JWT |
| `GET` | `/api/users/{id}` | Yes | Get user profile |
| `PUT` | `/api/users/{id}` | Yes | Update user profile |
| `DELETE` | `/api/users/{id}` | Yes | Delete user account |

### Restaurant Service — `/api/restaurants`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/restaurants` | Yes | Create restaurant (owner) |
| `GET` | `/api/restaurants` | No | List restaurants (paginated) |
| `GET` | `/api/restaurants/{id}` | No | Get restaurant by ID |
| `PUT` | `/api/restaurants/{id}` | Yes | Update restaurant (owner) |
| `DELETE` | `/api/restaurants/{id}` | Yes | Delete restaurant (owner) |

Query params for listing: `pageNo` (default `0`), `pageSize` (default `10`)

### Menu Items — `/api/menu-items`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/menu-items` | Yes | Create menu item (owner) |
| `GET` | `/api/menu-items` | No | List menu items (paginated) |
| `GET` | `/api/menu-items/{id}` | No | Get menu item by ID |
| `PUT` | `/api/menu-items/{id}` | Yes | Update menu item (owner) |
| `DELETE` | `/api/menu-items/{id}` | Yes | Delete menu item (owner) |

### Order Service — `/api/orders`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/orders/create` | Yes | Place a new order |
| `GET` | `/api/orders/{orderId}` | Yes | Get order details (includes delivery OTP) |
| `GET` | `/api/orders/all` | Yes | List user's orders (paginated) |
| `PUT` | `/api/orders/{orderId}/status` | Yes | Update order status (restaurant owner) |
| `DELETE` | `/api/orders/{orderId}` | Yes | Cancel/delete order |
| `POST` | `/api/orders/{orderId}/verify-delivery` | Yes | Verify delivery OTP and complete order |
| `PUT` | `/api/orders/{orderId}/payment-success` | Internal | Called by Payment Service after verification |

Query params for listing: `pageNumber` (default `0`), `pageSize` (default `10`)

**Order statuses:** `PENDING` → `CONFIRMED` → `PREPARING` → `OUT_FOR_DELIVERY` → `DELIVERED` (or `CANCELLED`)

**Payment methods:** `COD`, `UPI`, `CARD`, `NET_BANKING`

### Payment Service — `/api/payments`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/payments/create-order` | Yes | Create a Razorpay order |
| `POST` | `/api/payments/verify` | Yes | Verify Razorpay payment signature |

---

## Example Requests

### Register a user
```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "password123",
    "role": "CUSTOMER",
    "phoneNumber": "9876543210",
    "address": "123 Main St",
    "gender": "Male"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'
```

### List restaurants
```bash
curl http://localhost:8080/api/restaurants?pageNo=0&pageSize=10
```

### Create an order
```bash
curl -X POST http://localhost:8080/api/orders/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -d '{
    "userId": 1,
    "restaurantId": 1,
    "deliveryAddress": "123 Main St",
    "paymentMethod": "UPI",
    "paymentStatus": "PENDING",
    "items": [
      { "menuItemId": 1, "quantity": 2 }
    ]
  }'
```

### Verify Razorpay payment
```bash
curl -X POST http://localhost:8080/api/payments/verify \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -d '{
    "razorpayOrderId": "order_xxx",
    "razorpayPaymentId": "pay_xxx",
    "razorpaySignature": "signature_xxx"
  }'
```

---

## Project Structure

```
Food_Court_Microservices/
├── eureka-server/          # Service discovery (port 8761)
├── config_server/          # Centralized config from Git (port 8888)
├── api-gateway/            # API Gateway with JWT filter (port 8080)
├── auth-service/           # Authentication & user management (port 8081)
├── restaurant-service/     # Restaurants & menu items (port 8082)
├── order-service/          # Order processing (port 8083)
├── payment-service/        # Razorpay payments (port 8084)
└── README.md
```

Each service follows a standard Spring Boot layout:

```
src/main/java/
├── controller/       # REST endpoints
├── service/          # Business logic
├── repository/       # JPA data access
├── entity/           # Database entities
├── dto/              # Request/response DTOs
├── client/           # OpenFeign clients (Order, Payment)
├── clientFallback/   # Feign fallback handlers
├── security/         # JWT filters and utilities
├── config/           # Configuration classes
└── exception/        # Global exception handlers
```

---

## Inter-Service Communication

| From | To | Mechanism | Purpose |
|------|----|-----------|---------|
| Order Service | Restaurant Service | OpenFeign (`RESTAURANT-SERVICE`) | Validate restaurant and menu items |
| Order Service | Payment Service | OpenFeign (`PAYMENT-SERVICE`) | Create Razorpay order |
| Payment Service | Order Service | OpenFeign (`ORDER-SERVICE`) | Mark payment as successful |
| All services | Eureka | Eureka Client | Service registration and discovery |
| All services | Config Server | Spring Cloud Config | Load ports, DB, secrets, gateway routes |

Feign clients include **fallback classes** that throw errors when a downstream service is unavailable. Internal calls from Payment to Order use the `X-Internal-Token` header for authentication.

---

## Security

- **API Gateway** validates JWT on all routes except registration and login
- **Auth Service** uses Spring Security with BCrypt password encoding
- **Order & Restaurant Services** extract `user_id` and `role` from JWT via custom filters
- **IDOR protection** — users can only access/modify their own profile
- **Owner-scoped operations** — only restaurant owners can modify their restaurants, menu items, and order statuses
- **Internal token** — `secret.token` secures service-to-service payment callbacks
- JWT claims: `user_id`, `role`, `sub` (email); 24-hour expiration

---

## Configuration

Local `application.yml` files only contain the service name and Config Server import. All runtime configuration (ports, databases, JWT keys, Razorpay credentials, gateway routes) lives in the external Git config repository.

To refresh configuration at runtime without restarting:
```bash
curl -X POST http://localhost:<port>/actuator/refresh
```

---

## Monitoring

Services expose Spring Boot Actuator endpoints:

```bash
curl http://localhost:<port>/actuator/health
```

View registered services on the Eureka dashboard: **http://localhost:8761**

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Services not in Eureka | Start Eureka first; verify `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` in config |
| Config Server fails to start | Set `CONFIG_REPO_PAT` with a valid GitHub token |
| 401 / invalid token | Ensure `secret.key` is identical across Auth, Gateway, Order, and Restaurant services |
| Payment verification fails | Check Razorpay key ID/secret in config; verify callback signature fields |
| Order creation fails | Confirm restaurant is open, menu items are available, and items belong to the restaurant |
| Port already in use | `netstat -ano \| findstr :<port>` then `taskkill /PID <pid> /F` (Windows) |
| Maven build fails | Run `mvn clean install -U`; confirm Java 21 is active |

---

## Getting Started Checklist

- [ ] Install Java 21 and Maven
- [ ] Set up MySQL databases (per service, via config repo)
- [ ] Create a GitHub PAT and set `CONFIG_REPO_PAT`
- [ ] Configure Razorpay keys in the config repository
- [ ] Build all services with `mvn clean install`
- [ ] Start Eureka Server (8761)
- [ ] Start Config Server (8888)
- [ ] Start Auth Service (8081)
- [ ] Start Restaurant Service (8082)
- [ ] Start Order Service (8083)
- [ ] Start Payment Service (8084)
- [ ] Start API Gateway (8080)
- [ ] Verify services on Eureka dashboard
- [ ] Test registration, login, and order flow

---

## Resources

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Spring Cloud](https://spring.io/projects/spring-cloud)
- [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway)
- [Netflix Eureka](https://github.com/Netflix/eureka)
- [Spring Cloud Config](https://spring.io/projects/spring-cloud-config)
- [Razorpay Docs](https://razorpay.com/docs/)

---

**Last Updated:** June 2025
