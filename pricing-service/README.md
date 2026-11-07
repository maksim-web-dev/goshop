# Pricing Service

Microservice responsible for managing product prices, store-specific discounts, currency exchange rates, and price change history within the GoShop platform.

---

## Table of Contents

1. [Service Overview](#1-service-overview)
2. [Technology Stack](#2-technology-stack)
3. [Database Model](#3-database-model)
4. [REST API Endpoints](#4-rest-api-endpoints)
5. [Business Logic](#5-business-logic)
6. [Inter-Service Communication](#6-inter-service-communication)
7. [Kafka and Asynchronous Events](#7-kafka-and-asynchronous-events)
8. [Error Handling](#8-error-handling)
9. [Configuration and Deployment](#9-configuration-and-deployment)
10. [Testing](#10-testing)
11. [Known Limitations and Missing Functionality](#11-known-limitations-and-missing-functionality)

---

## 1. Service Overview

**Purpose:** The pricing-service is the single source of truth for all monetary data in the GoShop platform.

**Responsibilities:**
- Storing base platform prices and store-specific prices per product
- Managing time-bound discounts (percentage and fixed-amount) scoped to a store or a specific product within a store
- Storing currency exchange rates with historical tracking (`effectiveAt`)
- Recording price change history automatically whenever a price `amount` is modified
- Computing final prices for a given `(productId, storeId, currencyCode)` tuple by applying the best available active discount

**Data ownership:**
- `prices` table — owned exclusively by this service; `productId` values are foreign references to `product-service` (no FK enforced at DB level)
- `discounts` table — owned exclusively by this service; `storeId` values are foreign references to `store-service` (no FK enforced)
- `exchange_rates` table — static reference data, populated via seed; no external owner
- `price_history` table — append-only audit log, written automatically by `PriceService.update()`

---

## 2. Technology Stack

| Component | Version / Artifact |
|---|---|
| Language | Kotlin 2.0.0 |
| JVM target | Java 21 (`jvmToolchain(21)`) |
| Spring Boot | 3.3.5 |
| Spring Dependency Management | 1.1.6 |
| Kotlin Spring plugin | 2.0.0 |
| Kotlin JPA plugin | 2.0.0 |
| Spring Web (MVC) | `spring-boot-starter-web` |
| Spring Data JPA | `spring-boot-starter-data-jpa` |
| Spring Security | `spring-boot-starter-security` |
| Jackson Kotlin module | `jackson-module-kotlin` (managed by Boot) |
| SpringDoc OpenAPI (Swagger UI) | `springdoc-openapi-starter-webmvc-ui:2.6.0` |
| PostgreSQL JDBC driver | `org.postgresql:postgresql` (runtime) |
| Test | `spring-boot-starter-test` |
| ORM / connection pool | Hibernate + HikariCP (via Spring Data JPA) |
| Build tool | Gradle (wrapper `gradlew`) |
| Docker base images | `gradle:8.10-jdk21` (build), `eclipse-temurin:21-jre-alpine` (runtime) |

---

## 3. Database Model

### 3.1 `prices`

| Column | Java type | DB constraints | Notes |
|---|---|---|---|
| `id` | `Long` | PK, NOT NULL | Auto-generated (`GenerationType.IDENTITY`) |
| `product_id` | `Long` | NOT NULL | Reference to product-service (no DB FK) |
| `store_id` | `Long` | nullable | `null` = platform base price; non-null = store-specific price |
| `amount` | `BigDecimal` | NOT NULL, precision=19, scale=4 | Stored price value |
| `currency_code` | `String` | NOT NULL, length=3 | ISO 4217 code, e.g. `USD` |
| `valid_from` | `Instant` | NOT NULL | Defaults to `Instant.now()` on creation |
| `valid_to` | `Instant` | nullable | `null` = no expiry |
| `updated_at` | `Instant` | NOT NULL | Updated explicitly on every `PriceService.update()` call |

### 3.2 `discounts`

| Column | Java type | DB constraints | Notes |
|---|---|---|---|
| `id` | `Long` | PK, NOT NULL | Auto-generated (`GenerationType.IDENTITY`) |
| `store_id` | `Long` | NOT NULL | Store this discount belongs to |
| `product_id` | `Long` | nullable | `null` = applies to entire store catalogue |
| `discount_type` | `DiscountType` (enum) | NOT NULL | Stored as `STRING`; values: `PERCENTAGE`, `FIXED` |
| `discount_value` | `BigDecimal` | NOT NULL, precision=19, scale=4 | For `PERCENTAGE`: 0–100; for `FIXED`: monetary amount in store currency |
| `starts_at` | `Instant` | NOT NULL | Discount activation time |
| `ends_at` | `Instant` | nullable | `null` = no expiry |
| `status` | `DiscountStatus` (enum) | NOT NULL | Stored as `STRING`; default `ACTIVE`; values: `ACTIVE`, `INACTIVE`, `EXPIRED` |

### 3.3 `exchange_rates`

| Column | Java type | DB constraints | Notes |
|---|---|---|---|
| `id` | `Long` | PK, NOT NULL | Auto-generated (`GenerationType.IDENTITY`) |
| `from_currency` | `String` | NOT NULL, length=3 | ISO 4217 source currency |
| `to_currency` | `String` | NOT NULL, length=3 | ISO 4217 target currency |
| `rate` | `BigDecimal` | NOT NULL, precision=19, scale=8 | Conversion multiplier |
| `effective_at` | `Instant` | NOT NULL | Defaults to `Instant.now()` on creation |

### 3.4 `price_history`

| Column | Java type | DB constraints | Notes |
|---|---|---|---|
| `id` | `Long` | PK, NOT NULL | Auto-generated (`GenerationType.IDENTITY`) |
| `price_id` | `Long` (FK) | NOT NULL | `@ManyToOne(LAZY)` → `prices.id` |
| `old_amount` | `BigDecimal` | NOT NULL, precision=19, scale=4 | Value before the update |
| `new_amount` | `BigDecimal` | NOT NULL, precision=19, scale=4 | Value after the update |
| `changed_at` | `Instant` | NOT NULL, `updatable=false` | Defaults to `Instant.now()` on insert |

### 3.5 Enum types

**`DiscountType`** — `PERCENTAGE` | `FIXED`

**`DiscountStatus`** — `ACTIVE` | `INACTIVE` | `EXPIRED`

---

## 4. REST API Endpoints

All paths below are relative to the context-path `/api`.
Full URL structure: `http://host:8084/api/<path>`

Dates are serialized as ISO-8601 strings (e.g. `"2024-01-15T10:00:00Z"`) because `write-dates-as-timestamps: false` is set in `application.yml`.

`BigDecimal` fields (`amount`, `discountValue`, `rate`, etc.) are serialized as **JSON numbers** by default Jackson behavior — no custom serializer is registered.

---

### 4.1 Price Endpoints — `PriceController` (`/prices`)

---

#### GET /prices/products/{productId}

- **Handler:** `PriceController.getByProduct`
- **Purpose:** Returns all currently-active prices for a given product (base price and all store-specific prices).
- **Path variables:** `productId` — `UUID`
- **Query parameters:** none
- **Request body:** none
- **Response body:** `List<PriceResponse>` — `200 OK`

**`PriceResponse` fields:**

| Field | Type | Nullable |
|---|---|---|
| `id` | `Long` | no |
| `productId` | `Long` | no |
| `storeId` | `Long` | yes (`null` = base price) |
| `amount` | `BigDecimal` | no |
| `currencyCode` | `String` | no |
| `validFrom` | `Instant` (ISO-8601 string) | no |
| `validTo` | `Instant` (ISO-8601 string) | yes |
| `updatedAt` | `Instant` (ISO-8601 string) | no |

**Active price filter (applied by `PriceRepository`):**
Only prices where `validTo IS NULL OR validTo > now()` are returned, ordered by `storeId NULLS LAST, validFrom DESC`.

**Example response:**
```json
[
  {
    "id": 1,
    "productId": 1001,
    "storeId": null,
    "amount": 199.9900,
    "currencyCode": "USD",
    "validFrom": "2024-01-01T00:00:00Z",
    "validTo": null,
    "updatedAt": "2024-06-01T12:00:00Z"
  },
  {
    "id": 2,
    "productId": 1001,
    "storeId": 501,
    "amount": 189.9900,
    "currencyCode": "USD",
    "validFrom": "2024-03-01T00:00:00Z",
    "validTo": null,
    "updatedAt": "2024-06-01T12:00:00Z"
  }
]
```

**Possible errors:** none declared (empty list returned if no active prices exist)

---

#### POST /prices

- **Handler:** `PriceController.create`
- **Purpose:** Creates a new price record.
- **Request body:** `PriceCreateRequest`
- **Response body:** `PriceResponse` — `201 Created`

**`PriceCreateRequest` fields:**

| Field | Type | Required | Notes |
|---|---|---|---|
| `productId` | `Long` | yes | |
| `storeId` | `Long` | no | Omit or `null` for a base platform price |
| `amount` | `BigDecimal` | yes | |
| `currencyCode` | `String` | yes | ISO 4217, 3 chars |
| `validFrom` | `Instant` (ISO-8601) | no | Defaults to current server time |
| `validTo` | `Instant` (ISO-8601) | no | Omit for open-ended price |

**Example request:**
```json
{
  "productId": 1001,
  "storeId": 501,
  "amount": 189.99,
  "currencyCode": "USD",
  "validFrom": "2024-03-01T00:00:00Z"
}
```

**Example response:** same shape as `PriceResponse` above with `201 Created`.

**Possible errors:** none declared at application level (Jackson deserialization errors return `400` from Spring)

---

#### PUT /prices/{id}

- **Handler:** `PriceController.update`
- **Purpose:** Partially updates an existing price. If `amount` changes, a `PriceHistory` record is automatically persisted.
- **Path variables:** `id` — `Long`
- **Request body:** `PriceUpdateRequest` — all fields are optional

**`PriceUpdateRequest` fields:**

| Field | Type | Required | Notes |
|---|---|---|---|
| `amount` | `BigDecimal` | no | Triggers history write if changed |
| `currencyCode` | `String` | no | |
| `validFrom` | `Instant` (ISO-8601) | no | |
| `validTo` | `Instant` (ISO-8601) | no | |

- **Response body:** `PriceResponse` — `200 OK`

**Example request:**
```json
{
  "amount": 175.00,
  "validTo": "2025-12-31T23:59:59Z"
}
```

**Possible errors:**
- `404 Not Found` — price with given `id` does not exist

---

#### GET /prices/{id}/history

- **Handler:** `PriceController.getHistory`
- **Purpose:** Returns the price change history for a price record, most recent first.
- **Path variables:** `id` — `Long`
- **Query parameters:** none
- **Request body:** none
- **Response body:** `List<PriceHistoryResponse>` — `200 OK`

**`PriceHistoryResponse` fields:**

| Field | Type | Nullable |
|---|---|---|
| `id` | `Long` | no |
| `priceId` | `Long` | no |
| `oldAmount` | `BigDecimal` | no |
| `newAmount` | `BigDecimal` | no |
| `changedAt` | `Instant` (ISO-8601 string) | no |

**Example response:**
```json
[
  {
    "id": 1,
    "priceId": 2,
    "oldAmount": 189.9900,
    "newAmount": 175.0000,
    "changedAt": "2024-07-10T09:30:00Z"
  }
]
```

**Possible errors:** returns empty list if no history; no `404` guard for the price itself

---

#### POST /prices/batch

- **Handler:** `PriceController.getBatch`
- **Purpose:** Returns all price records (not filtered by `validTo`) for a list of product IDs. Uses `findByProductIdIn` — no active-price filter applied.
- **Request body:** `BatchPriceRequest`

**`BatchPriceRequest` fields:**

| Field | Type | Required |
|---|---|---|
| `productIds` | `List<Long>` | yes |

- **Response body:** `List<PriceResponse>` — `200 OK`

**Example request:**
```json
{
  "productIds": [1001, 1002]
}
```

**Possible errors:** none declared

---

#### POST /prices/calculate

- **Handler:** `PriceController.calculate`
- **Purpose:** Computes the final price for a product in a given store and currency, applying the best active discount if available.
- **Request body:** `CalculatePriceRequest`

**`CalculatePriceRequest` fields:**

| Field | Type | Required | Notes |
|---|---|---|---|
| `productId` | `Long` | yes | |
| `storeId` | `Long` | no | If `null`, discounts are not looked up |
| `currencyCode` | `String` | yes | ISO 4217 code |

- **Response body:** `CalculatePriceResponse` — `200 OK`

**`CalculatePriceResponse` fields:**

| Field | Type | Nullable |
|---|---|---|
| `originalAmount` | `BigDecimal` | no |
| `finalAmount` | `BigDecimal` | no |
| `currencyCode` | `String` | no |
| `discountApplied` | `Boolean` | no |

**Example request:**
```json
{
  "productId": 1001,
  "storeId": 501,
  "currencyCode": "USD"
}
```

**Example response (with discount applied):**
```json
{
  "originalAmount": 189.9900,
  "finalAmount": 161.4165,
  "currencyCode": "USD",
  "discountApplied": true
}
```

**Possible errors:**
- `404 Not Found` — no active price found for the requested `(productId, currencyCode)` / `(productId, storeId, currencyCode)` combination

---

### 4.2 Discount Endpoints — `DiscountController` (`/discounts`)

---

#### GET /discounts

- **Handler:** `DiscountController.getAll`
- **Purpose:** Returns all discount records regardless of status or date.
- **Request body:** none
- **Response body:** `List<DiscountResponse>` — `200 OK`

**`DiscountResponse` fields:**

| Field | Type | Nullable |
|---|---|---|
| `id` | `Long` | no |
| `storeId` | `Long` | no |
| `productId` | `Long` | yes (`null` = whole-store discount) |
| `discountType` | `String` (enum) | no — `PERCENTAGE` or `FIXED` |
| `discountValue` | `BigDecimal` | no |
| `startsAt` | `Instant` (ISO-8601 string) | no |
| `endsAt` | `Instant` (ISO-8601 string) | yes |
| `status` | `String` (enum) | no — `ACTIVE`, `INACTIVE`, or `EXPIRED` |

**Example response:**
```json
[
  {
    "id": 1,
    "storeId": 501,
    "productId": null,
    "discountType": "PERCENTAGE",
    "discountValue": 15.0000,
    "startsAt": "2024-01-01T00:00:00Z",
    "endsAt": "2024-12-31T23:59:59Z",
    "status": "ACTIVE"
  }
]
```

---

#### POST /discounts

- **Handler:** `DiscountController.create`
- **Purpose:** Creates a new discount. New discounts are always created with status `ACTIVE` (default entity value; status is not part of the create request).
- **Request body:** `DiscountCreateRequest`
- **Response body:** `DiscountResponse` — `201 Created`

**`DiscountCreateRequest` fields:**

| Field | Type | Required | Notes |
|---|---|---|---|
| `storeId` | `Long` | yes | |
| `productId` | `Long` | no | `null` = applies to entire store |
| `discountType` | `String` (enum) | yes | `PERCENTAGE` or `FIXED` |
| `discountValue` | `BigDecimal` | yes | 0–100 for `PERCENTAGE`; monetary for `FIXED` |
| `startsAt` | `Instant` (ISO-8601) | yes | |
| `endsAt` | `Instant` (ISO-8601) | no | Omit for open-ended discount |

**Example request:**
```json
{
  "storeId": 501,
  "discountType": "PERCENTAGE",
  "discountValue": 15.00,
  "startsAt": "2024-06-01T00:00:00Z",
  "endsAt": "2024-08-31T23:59:59Z"
}
```

---

#### PUT /discounts/{id}

- **Handler:** `DiscountController.update`
- **Purpose:** Partially updates an existing discount. `storeId`, `productId`, and `discountType` are immutable after creation.
- **Path variables:** `id` — `Long`
- **Request body:** `DiscountUpdateRequest` — all fields optional

**`DiscountUpdateRequest` fields:**

| Field | Type | Required |
|---|---|---|
| `discountValue` | `BigDecimal` | no |
| `startsAt` | `Instant` (ISO-8601) | no |
| `endsAt` | `Instant` (ISO-8601) | no |
| `status` | `String` (enum) | no — `ACTIVE`, `INACTIVE`, or `EXPIRED` |

- **Response body:** `DiscountResponse` — `200 OK`

**Possible errors:**
- `404 Not Found` — discount with given `id` does not exist

---

#### DELETE /discounts/{id}

- **Handler:** `DiscountController.disable`
- **Purpose:** Sets the discount status to `INACTIVE`. The record is not deleted from the database.
- **Path variables:** `id` — `Long`
- **Request body:** none
- **Response body:** none — `204 No Content`

**Possible errors:**
- `404 Not Found` — discount with given `id` does not exist

---

### 4.3 Exchange Rate Endpoints — `ExchangeRateController` (`/exchange-rates`)

---

#### GET /exchange-rates

- **Handler:** `ExchangeRateController.getAll`
- **Purpose:** Returns all exchange rate records stored in the database (all historical entries, not just the latest per pair).
- **Request body:** none
- **Response body:** `List<ExchangeRateResponse>` — `200 OK`

**`ExchangeRateResponse` fields:**

| Field | Type | Nullable |
|---|---|---|
| `id` | `Long` | no |
| `fromCurrency` | `String` | no |
| `toCurrency` | `String` | no |
| `rate` | `BigDecimal` | no |
| `effectiveAt` | `Instant` (ISO-8601 string) | no |

**Example response:**
```json
[
  {
    "id": 1,
    "fromCurrency": "USD",
    "toCurrency": "EUR",
    "rate": 0.92000000,
    "effectiveAt": "2024-01-01T00:00:00Z"
  }
]
```

---

## 5. Business Logic

### 5.1 `PriceService.calculate()` — Final Price Computation

**Location:** `com.service.price.PriceService`

**Input:** `CalculatePriceDto(productId: Long, storeId: Long?, currencyCode: String)`

**Return type:** `CalculatedPrice` data class

```
data class CalculatedPrice(
    val originalAmount: BigDecimal,
    val finalAmount: BigDecimal,
    val currencyCode: String,
    val discountApplied: Boolean
)
```

**Algorithm (step by step):**

1. Query `prices` for all active records matching `productId` (where `validTo IS NULL OR validTo > now()`), ordered `storeId NULLS LAST, validFrom DESC`.
2. Select the base price using the following fallback chain:
   - First preference: a price where `storeId == dto.storeId` AND `currencyCode == dto.currencyCode`
   - Second preference: a base platform price where `storeId IS NULL` AND `currencyCode == dto.currencyCode`
   - If neither is found: throw `ResponseStatusException(404)`
3. If `dto.storeId` is not null, query `discounts` for active discounts matching the store and product:
   - Conditions: `storeId = dto.storeId`, `(productId IS NULL OR productId = dto.productId)`, `status = 'ACTIVE'`, `startsAt <= now()`, `(endsAt IS NULL OR endsAt > now())`
   - Ordered by `discountValue DESC` — the highest-value discount wins
   - If `dto.storeId` is `null`, no discount lookup is performed (empty list)
4. Take only the first (highest-value) discount from the result list.
5. Apply the discount:
   - `PERCENTAGE`: `finalAmount = amount * (1 - discountValue / 100)`, then `.setScale(4, RoundingMode.HALF_UP)`
   - `FIXED`: `finalAmount = (amount - discountValue).coerceAtLeast(BigDecimal.ZERO)` — no explicit scale rounding applied for FIXED type
6. If no discount exists, `finalAmount = originalAmount` unchanged.
7. Return `CalculatedPrice` with `discountApplied = (best != null)`.

**Note:** When multiple discounts are eligible, the one with the highest `discountValue` wins. This means a 50% discount always beats a 10% discount regardless of which is more beneficial for the customer in absolute terms (e.g. a $5 fixed discount on a $3 item still loses to a 1% percentage discount because `discountValue` ordering is numeric, not by effective saving).

### 5.2 `PriceService.update()` — Automatic History Recording

When `PriceService.update()` is called with a non-null `dto.amount` that differs from the existing amount:
- A new `PriceHistory` record is inserted with `oldAmount = existing.amount` and `newAmount = dto.amount`
- The price entity is then replaced via `save()` with a new `Price` instance carrying `updatedAt = Instant.now()`
- Both operations run inside a single `@Transactional` method

### 5.3 `ExchangeRateService.getRate()`

Returns `BigDecimal.ONE` immediately if `from == to`. Otherwise queries for the latest rate record where `effectiveAt <= now()`. Throws `404` if no rate is found. This method is **not called from any controller** — it is an internal utility that is currently unused by the REST layer.

### 5.4 `DiscountService.disable()`

Sets `status = DiscountStatus.INACTIVE` on the target discount. Does not set `endsAt` or verify that `startsAt` is in the past. The `EXPIRED` status is defined in the enum but is never set programmatically — it would need to be set either manually via `PUT /discounts/{id}` or by a scheduled job (not implemented).

---

## 6. Inter-Service Communication

No inter-service communication is currently implemented. The service does not make any HTTP calls to other services. `productId` and `storeId` values stored in the database are externally-provided `Long` IDs with no runtime validation against `product-service` or `store-service`.

---

## 7. Kafka and Asynchronous Events

Kafka is not implemented. The `build.gradle.kts` file contains no Kafka dependency. No `@KafkaListener`, `@KafkaHandler`, or `KafkaTemplate` usages exist anywhere in the codebase.

---

## 8. Error Handling

No `@ControllerAdvice` or `@ExceptionHandler` class is implemented in this service. Error handling relies entirely on Spring Boot's default mechanism:

- `ResponseStatusException` thrown in service methods is translated by Spring MVC to the specified HTTP status code and its `reason` string as the response body.
- Jackson deserialization failures (malformed JSON, wrong types) produce a `400 Bad Request` from Spring's default error handling.
- Unhandled exceptions produce a `500 Internal Server Error`.

**HTTP status codes in use:**

| Scenario | Status |
|---|---|
| Successful GET / PUT / batch POST | `200 OK` |
| Successful POST (create) | `201 Created` |
| Successful DELETE (disable) | `204 No Content` |
| Price or discount not found | `404 Not Found` |
| No active price for calculate | `404 Not Found` |
| No exchange rate found | `404 Not Found` |

**Error response format (Spring Boot default `/error` endpoint):**
```json
{
  "timestamp": "2024-07-10T09:30:00.000+00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Price 999 not found",
  "path": "/api/prices/999"
}
```

---

## 9. Configuration and Deployment

### 9.1 Application Settings

| Env variable | Default | Description |
|---|---|---|
| `PRICING_SERVER_PORT` | `8084` | HTTP port the service listens on |
| `PRICING_API_PREFIX` | `/api` | Servlet context path (all endpoints prefixed with this) |
| `PRICING_POSTGRES_HOST` | `localhost` | PostgreSQL hostname |
| `PRICING_POSTGRES_PORT` | `5432` | PostgreSQL port |
| `PRICING_POSTGRES_DB` | _(required)_ | Database name |
| `PRICING_POSTGRES_USER` | _(required)_ | Database username |
| `PRICING_POSTGRES_PASSWORD` | _(required)_ | Database password |
| `PRICING_HIKARI_POOL_MAX` | `25` | HikariCP maximum pool size |
| `PRICING_HIKARI_POOL_MIN` | `2` | HikariCP minimum idle connections |
| `PRICING_HIKARI_POOL_TIMEOUT_MILLIS` | `30000` | HikariCP connection timeout (ms) |
| `PRICING_POSTGRES_SHOW_SQL` | `true` | Log Hibernate SQL statements |
| `PRICING_POSTGRES_DDL_AUTO` | `update` | Hibernate DDL mode (`update`, `create`, `none`, etc.) |
| `PRICING_SEED_ENABLE` | `false` | Enable data seeding on startup |
| `PRICING_SEED_MODE` | `init` | Seed data directory under `static/data/` |
| `PRICING_SEED_STOP_COUNT` | `1` | Skip seed if table already has more than this many rows |

SSL for the JDBC connection is hardcoded to `disable` in `PostgresConfig` (`sslmode=disable`).

### 9.2 OpenAPI / Swagger UI

| Endpoint | URL |
|---|---|
| OpenAPI JSON spec | `http://localhost:8084/api/v3/api-docs` |
| Swagger UI | `http://localhost:8084/api/swagger-ui.html` |

### 9.3 Security

Spring Security is included but configured to **permit all requests** (`anyRequest().permitAll()`). CSRF is disabled. Session management is stateless. No authentication or authorization is enforced.

### 9.4 Data Seeding

Three `CommandLineRunner` beans seed initial data from JSON files in `src/main/resources/static/data/{mode}/`:

| Order | Bean | File | Table |
|---|---|---|---|
| 1 | `ExchangeRateDataSeed` | `exchange_rate.json` | `exchange_rates` |
| 2 | `PriceDataSeed` | `price.json` | `prices` |
| 3 | `DiscountDataSeed` | `discount.json` | `discounts` |

The seed JSON for `prices` reads `validFrom` and `validTo` fields from each record. The seed JSON for `exchange_rates` reads the `effectiveAt` field from each record.

Each seed runner skips if `PRICING_SEED_ENABLE=false` or if the table row count exceeds `PRICING_SEED_STOP_COUNT`.

### 9.5 Docker

**Dockerfile** (multi-stage build):
- Stage 1: `gradle:8.10-jdk21` — runs `./gradlew :pricing-service:bootJar --no-daemon -x test`
- Stage 2: `eclipse-temurin:21-jre-alpine` — copies the built JAR, exposes port `8084`

**docker-compose.yml entry:**

```yaml
pricing-service:
  build:
    context: .
    dockerfile: pricing-service/Dockerfile
  container_name: pricing-service
  ports:
    - "8084:8084"
  environment:
    PRICING_POSTGRES_HOST: postgres-pricing
    PRICING_POSTGRES_PORT: 5432
    PRICING_POSTGRES_DB: pricing_db
    PRICING_POSTGRES_USER: postgres
    PRICING_POSTGRES_PASSWORD: postgres
    PRICING_POSTGRES_DDL_AUTO: update
    PRICING_POSTGRES_SHOW_SQL: "false"
    PRICING_SEED_ENABLE: "false"
  depends_on:
    postgres-pricing:
      condition: service_healthy
  networks:
    - goshop-network
```

The corresponding `postgres-pricing` service runs `postgres:16-alpine` on host port `5435` (mapped to container port `5432`).

### 9.6 Running Locally

```bash
# Start the database
docker compose up postgres-pricing -d

# Copy and fill environment variables
cp pricing-service/.env.sample .env
# Set PRICING_POSTGRES_DB, PRICING_POSTGRES_USER, PRICING_POSTGRES_PASSWORD

# Run the service
./gradlew :pricing-service:bootRun
```

---

## 10. Testing

No test files exist under `src/test/`. The `spring-boot-starter-test` dependency is declared in `build.gradle.kts` but no test classes have been written.

---

## 11. Known Limitations and Missing Functionality

### ✅ IMPLEMENTED

- CRUD for prices with active-price filtering and time-bound validity
- Automatic `PriceHistory` recording when price `amount` changes
- CRUD for discounts with soft-delete (status → `INACTIVE`) instead of hard delete
- Price calculation with `PERCENTAGE` and `FIXED` discount types, best-discount selection by highest `discountValue`
- Exchange rate storage and retrieval (read-only via REST; lookup via `ExchangeRateService.getRate()`)
- Data seeding from bundled JSON files
- Swagger UI / OpenAPI spec endpoint
- HikariCP connection pool with configurable limits

### ⚠️ PARTIALLY IMPLEMENTED

- **Exchange rate usage in price calculation:** `ExchangeRateService.getRate()` exists and is correct, but `PriceService.calculate()` does not call it. If the requested `currencyCode` does not match a stored price's `currencyCode`, a `404` is thrown instead of converting the amount using an exchange rate.
- **Discount `EXPIRED` status:** The `EXPIRED` enum value is defined but never set by any service method. Expiry detection must be done manually via `PUT /discounts/{id}` with `status: "EXPIRED"` or inferred at query time by `endsAt`.
- **`GET /discounts` filtering:** The endpoint returns all discounts via `findAll()`. The existing README listed `storeId` and `productId` query parameters, but these are not implemented — `DiscountController.getAll()` accepts no parameters.
- **`Main.kt` stub file:** A leftover IDE-generated `Main.kt` with a `fun main()` hello-world print exists in `src/main/kotlin/com/`. It compiles but serves no purpose.

### ❌ NOT IMPLEMENTED

- **No `@ControllerAdvice` / global error handler:** Error responses use Spring Boot's default `/error` endpoint format; no custom structured error body.
- **No authentication or authorization:** Spring Security is configured to permit all requests.
- **No Kafka event publishing:** Price updates, discount creation, and price calculations do not emit any events to other services.
- **No inter-service HTTP clients:** No `RestTemplate`, `WebClient`, or Feign client. Product and store existence is not validated.
- **No exchange-rate auto-refresh:** Exchange rates are static seed data; there is no scheduler or external API integration to keep them current.
- **No `DELETE` endpoint for prices:** Prices can only be expired by setting `validTo` via `PUT /prices/{id}`.
- **No `GET /discounts/{id}` endpoint:** Individual discount lookup by ID exists in `DiscountService` but is not exposed via a controller method.
- **No tests:** Zero test classes exist despite the test dependency being declared.
- **No currency conversion in `/prices/calculate`:** The `currencyCode` in the request must exactly match a stored price's `currencyCode`; no conversion is applied.
- **No pagination:** All list endpoints return unbounded results; large datasets will return everything in a single response.
