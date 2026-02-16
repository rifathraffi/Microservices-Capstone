# Capstone Microservices

Spring Boot 4 microservices project with Java 17 (records, sealed types, text blocks).

## Architecture

- **Service Registry (Eureka)** – port 8761 – Discovery server
- **API Gateway** – port 8020 – Spring Cloud Gateway, JWT auth, routing, logging
- **Auth Service** – port 8083 – JWT token issuance (login, register)
- **Product Catalog Service** – port 8081 – Spring Data JPA, PostgreSQL
- **Order Management Service** – port 8082 – Order lifecycle, async communication

## Prerequisites

- Java 17+
- Maven 3.9+
- PostgreSQL (default: localhost:5432)

## PostgreSQL Setup

Create databases:

```sql
CREATE DATABASE productcatalog;
CREATE DATABASE ordermanagement;
```

Default credentials: `postgres/postgres`. Override in `application.yml` per service.

## Running in STS (Spring Tool Suite)

1. Import as Maven project: File → Import → Maven → Existing Maven Projects
2. Select the root folder `Final Project - Capstone`
3. Ensure Java 17 is configured
4. Start in this order:
   - **service-registry** (Eureka)
   - **api-gateway**
   - **auth-service**
   - **product-catalog-service**
   - **order-management-service**

## API Endpoints (via Gateway)

Base URL: `http://localhost:8020`

| Path | Service | Auth |
|------|---------|------|
| `/api/v1/products/**` | Product Catalog | JWT |
| `/api/v1/orders/**` | Order Management | JWT |
| `/api/v1/auth/**` | Auth Service | No |

### Get JWT Token

**Login** (demo users: `admin`/`admin123`, `user`/`user123`):

```bash
curl -X POST http://localhost:8020/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

**Register** (creates user and returns token):

```bash
curl -X POST http://localhost:8020/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"newuser","password":"password123"}'
```

Response: `{"token":"eyJ...","username":"admin","role":"ADMIN","type":"Bearer"}` — use the `token` value in the `Authorization` header.

### Role-Based Access

| Role | Demo user | Permissions |
|------|-----------|-------------|
| **ADMIN** | admin/admin123 | Full access (products, orders, list by customer) |
| **USER** | user/user123 | Create order, GET order by ID/number; **cannot** list orders by customer |

- **USER** calling `GET /api/v1/orders?customerId=xxx` returns **403 Forbidden**
- **ADMIN** can access all endpoints
- New registrations receive **USER** role

### Example: Create Product

```bash
# First obtain a JWT from your auth service, then:
curl -X POST http://localhost:8020/api/v1/products \
  -H "Authorization: Bearer <JWT>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Widget","description":"A widget","price":19.99,"quantity":100,"sku":"WID-001"}'
```

### Example: Create Order

```bash
curl -X POST http://localhost:8020/api/v1/orders \
  -H "Authorization: Bearer <JWT>" \
  -H "Content-Type: application/json" \
  -d '{"customerId":"user1","items":[{"sku":"WID-001","quantity":2,"unitPrice":19.99}]}'
```

## Direct Access (without Gateway)

- Product Catalog: http://localhost:8081/api/v1/products
- Order Management: http://localhost:8082/api/v1/orders

## Java 17 Features Used

- **Records**: `ProductDto`, `OrderDto`, `OrderItemDto`, `CreateOrderRequest`, `InventorySuccess`, `InventoryError`
- **Sealed types**: `InventoryResult` (permits `InventorySuccess`, `InventoryError`), `OrderStatus`
- **Text blocks**: Structured log messages in `ProductService`, `OrderService`
