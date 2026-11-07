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
- Batch price calculation with optional currency conversion for up to 100 items in a single request

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
| Spring Validation | `spring-boot-starter-validation` |
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

**Indexes on `prices`:**

| Index name | Columns | Purpose |
|---|---|---|
| `idx_prices_product_id` | `product_id` | Active-price lookup by product |
| `idx_prices_store_id` | `store_id` | Filter by store |
| `idx_prices_currency_code` | `currency_code` | Filter by currency |
| `idx_prices_valid_to` | `valid_to` | Active-price range filter (`validTo IS NULL OR validTo > now`) |

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

**Indexes on `discounts`:**

| Index name | Columns | Purpose |
|---|---|---|
| `idx_discounts_store_id` | `store_id` | Discount lookup by store |
| `idx_discounts_product_id` | `product_id` | Discount lookup by product |
| `idx_discounts_status` | `status` | Filter active discounts |
| `idx_discounts_starts_at` | `starts_at` | Date-range filter on activation time |

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
- **Path variables:** `productId` — `Long`
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

**Possible errors:**
- `404 Not Found` — price with given `id` does not exist

---

#### GET /prices/{id}/history

- **Handler:** `PriceController.getHistory`
- **Purpose:** Returns the price change history for a price record, most recent first.
- **Path variables:** `id` — `Long`
- **Response body:** `List<PriceHistoryResponse>` — `200 OK`

**`PriceHistoryResponse` fields:**

| Field | Type | Nullable |
|---|---|---|
| `id` | `Long` | no |
| `priceId` | `Long` | no |
| `oldAmount` | `BigDecimal` | no |
| `newAmount` | `BigDecimal` | no |
| `changedAt` | `Instant` (ISO-8601 string) | no |

**Possible errors:** returns empty list if no history; no `404` guard for the price itself

---

#### POST /prices/batch

- **Handler:** `PriceController.getBatch`
- **Purpose:** Returns all currently-active price records for a list of product IDs in a single query. Uses `findCurrentByProductIdIn` — active-price filter (`validTo IS NULL OR validTo > now()`) is applied.
- **Request body:** `BatchPriceRequest`
- **Validation:** `productIds` must be non-empty and contain at most 100 items.

**`BatchPriceRequest` fields:**

| Field | Type | Required | Constraints |
|---|---|---|---|
| `productIds` | `List<Long>` | yes | `@NotEmpty`, `@Size(max=100)` |

- **Response body:** `List<PriceResponse>` — `200 OK`

**Example request:**
```json
{
  "productIds": [1001, 1002, 1003]
}
```

**Possible errors:**
- `400 Bad Request` — `productIds` is empty or exceeds 100 items

**N+1 prevention:** single JPQL `IN` query for all product IDs — `PriceRepository.findCurrentByProductIdIn()`.

---

#### POST /prices/calculate

- **Handler:** `PriceController.calculate`
- **Purpose:** Computes the final price for a single product in a given store and currency, applying the best active discount.
- **Request body:** `CalculatePriceRequest`

**`CalculatePriceRequest` fields:**

| Field | Type | Required | Notes |
|---|---|---|---|
| `productId` | `Long` | yes | |
| `storeId` | `Long` | no | If `null`, discounts are not looked up |
| `currencyCode` | `String` | yes | ISO 4217 code; must match a stored price's currency |

- **Response body:** `CalculatePriceResponse` — `200 OK`

**`CalculatePriceResponse` fields:**

| Field | Type | Nullable |
|---|---|---|
| `originalAmount` | `BigDecimal` | no |
| `finalAmount` | `BigDecimal` | no |
| `currencyCode` | `String` | no |
| `discountApplied` | `Boolean` | no |

**Possible errors:**
- `404 Not Found` — no active price found for the requested `(productId, currencyCode)` / `(productId, storeId, currencyCode)` combination

---

#### POST /prices/calculate/batch

- **Handler:** `PriceController.calculateBatch`
- **Purpose:** Computes final prices for up to 100 `(productId, storeId, currencyCode)` combinations in a single request. Optionally converts each result to a `targetCurrencyCode` using the latest available exchange rate. Products without a matching price are reported separately in `notFoundItems`.
- **Request body:** `BatchCalculateRequest`
- **Validation:** `items` must be non-empty and contain at most 100 items; each item's `currencyCode` and `targetCurrencyCode` must be exactly 3 characters.

**`BatchCalculateRequest` fields:**

| Field | Type | Required | Constraints |
|---|---|---|---|
| `items` | `List<BatchCalculateItem>` | yes | `@NotEmpty`, `@Size(max=100)` |

**`BatchCalculateItem` fields:**

| Field | Type | Required | Constraints | Notes |
|---|---|---|---|---|
| `productId` | `Long` | yes | `@Positive` | |
| `storeId` | `Long` | no | — | `null` = no store; discounts not applied |
| `currencyCode` | `String` | yes | `@NotBlank`, `@Size(min=3,max=3)` | Currency in which the price is stored |
| `targetCurrencyCode` | `String` | no | `@Size(min=3,max=3)` | If set, the result is converted to this currency |

- **Response body:** `BatchCalculateResponse` — `200 OK`

**`BatchCalculateResponse` fields:**

| Field | Type | Notes |
|---|---|---|
| `results` | `List<ResultItem>` | Successfully calculated items |
| `notFoundItems` | `List<NotFoundItem>` | Items for which no matching price was found |

**`ResultItem` fields:**

| Field | Type | Nullable |
|---|---|---|
| `productId` | `Long` | no |
| `storeId` | `Long` | yes |
| `originalAmount` | `BigDecimal` | no |
| `finalAmount` | `BigDecimal` | no |
| `currencyCode` | `String` | no — reflects `targetCurrencyCode` if conversion was applied |
| `discountApplied` | `Boolean` | no |

**`NotFoundItem` fields:**

| Field | Type |
|---|---|
| `productId` | `Long` |
| `storeId` | `Long` (nullable) |
| `currencyCode` | `String` |

**Example request:**
```json
{
  "items": [
    { "productId": 1001, "storeId": 501, "currencyCode": "USD" },
    { "productId": 1002, "storeId": 501, "currencyCode": "USD", "targetCurrencyCode": "EUR" },
    { "productId": 9999, "storeId": 501, "currencyCode": "USD" }
  ]
}
```

**Example response:**
```json
{
  "results": [
    {
      "productId": 1001,
      "storeId": 501,
      "originalAmount": 189.9900,
      "finalAmount": 161.4165,
      "currencyCode": "USD",
      "discountApplied": true
    },
    {
      "productId": 1002,
      "storeId": 501,
      "originalAmount": 184.7108,
      "finalAmount": 184.7108,
      "currencyCode": "EUR",
      "discountApplied": false
    }
  ],
  "notFoundItems": [
    { "productId": 9999, "storeId": 501, "currencyCode": "USD" }
  ]
}
```

**Possible errors:**
- `400 Bad Request` — validation failure (empty items, > 100 items, invalid currency code length)
- `404 Not Found` — a required exchange rate is not found for a `targetCurrencyCode` conversion (fails fast for the whole batch)

**N+1 prevention strategy:**
1. All prices loaded in a single `IN` query: `PriceRepository.findCurrentByProductIdIn(productIds, now)`.
2. All active discounts loaded in a single `IN` query: `DiscountRepository.findActiveForStores(storeIds, productIds, now)`.
3. Exchange rates per unique `(from, to)` pair are fetched once and cached in-memory for the duration of the call.
4. All remaining processing is in-memory.

---

### 4.2 Discount Endpoints — `DiscountController` (`/discounts`)

---

#### GET /discounts

- **Handler:** `DiscountController.getAll`
- **Purpose:** Returns all discount records regardless of status or date.
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

---

#### POST /discounts

- **Handler:** `DiscountController.create`
- **Purpose:** Creates a new discount with status `ACTIVE`.
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

---

#### PUT /discounts/{id}

- **Handler:** `DiscountController.update`
- **Purpose:** Partially updates an existing discount. `storeId`, `productId`, and `discountType` are immutable.
- **Path variables:** `id` — `Long`
- **Request body:** `DiscountUpdateRequest` — all fields optional
- **Response body:** `DiscountResponse` — `200 OK`

**Possible errors:**
- `404 Not Found` — discount with given `id` does not exist

---

#### DELETE /discounts/{id}

- **Handler:** `DiscountController.disable`
- **Purpose:** Sets the discount status to `INACTIVE`. Record is not deleted.
- **Path variables:** `id` — `Long`
- **Response body:** none — `204 No Content`

**Possible errors:**
- `404 Not Found` — discount with given `id` does not exist

---

### 4.3 Exchange Rate Endpoints — `ExchangeRateController` (`/exchange-rates`)

---

#### GET /exchange-rates

- **Handler:** `ExchangeRateController.getAll`
- **Purpose:** Returns all exchange rate records (all historical entries, not just the latest per pair).
- **Response body:** `List<ExchangeRateResponse>` — `200 OK`

**`ExchangeRateResponse` fields:**

| Field | Type | Nullable |
|---|---|---|
| `id` | `Long` | no |
| `fromCurrency` | `String` | no |
| `toCurrency` | `String` | no |
| `rate` | `BigDecimal` | no |
| `effectiveAt` | `Instant` (ISO-8601 string) | no |

---

## 5. Business Logic

### 5.1 `PriceService.calculate()` — Single-Item Price Computation

**Input:** `CalculatePriceDto(productId, storeId?, currencyCode)`

**Algorithm:**
1. Load all active prices for `productId` (single query, ordered `storeId NULLS LAST, validFrom DESC`).
2. Find base price: store-specific first, platform fallback second (same `currencyCode`). Throw `404` if not found.
3. If `storeId` is not null, load active discounts for the store + product. Take the discount with the highest `discountValue`.
4. Apply discount:
   - `PERCENTAGE`: `amount * (1 - discountValue / 100)`, scale=4 `HALF_UP`
   - `FIXED`: `(amount - discountValue).coerceAtLeast(0)`, scale=4 `HALF_UP`
5. Return `CalculatedPrice` with `discountApplied = (best != null)`.

> **Note:** When multiple discounts are eligible, the one with the highest `discountValue` wins (numeric ordering, not by effective saving).

### 5.2 `PriceService.calculateBatch()` — Batch Price Computation

**Input:** `BatchCalculatePriceDto` with up to 100 items, each `(productId, storeId?, currencyCode, targetCurrencyCode?)`

**Algorithm (N+1-free):**
1. Collect unique `productIds` and `storeIds` from all items.
2. Load all active prices in one `IN` query: `PriceRepository.findCurrentByProductIdIn(productIds, now)`. Group by `productId` in memory.
3. If any `storeId` values are present, load all active discounts in one `IN` query: `DiscountRepository.findActiveForStores(storeIds, productIds, now)`.
4. Identify all unique `(currencyCode → targetCurrencyCode)` conversion pairs. Prefetch exchange rates upfront — one query per unique pair, cached in a `Map`. Throw `404` immediately if any required rate is missing.
5. For each item:
   - Find base price (store-specific → platform fallback) in the in-memory map.
   - Find the best applicable discount in memory (filtered and sorted by `discountValue DESC`).
   - Apply discount (same `PERCENTAGE`/`FIXED` logic as `calculate()`).
   - If `targetCurrencyCode != currencyCode`, multiply both `originalAmount` and `finalAmount` by the cached exchange rate. The returned `currencyCode` reflects `targetCurrencyCode`.
   - Items without a matching price are added to `notFoundItems`.
6. Return `BatchCalculateResultDto(results, notFoundItems)`.

### 5.3 `PriceService.update()` — Automatic History Recording

When `amount` changes: a `PriceHistory` record is inserted with `oldAmount`/`newAmount`, then the price entity is replaced with `updatedAt = Instant.now()`. Both in a single `@Transactional` method.

### 5.4 `ExchangeRateService.getRate()`

Returns `BigDecimal.ONE` immediately if `from == to`. Otherwise queries for the latest rate record where `effectiveAt <= now()`. Throws `404` if no rate is found.

### 5.5 `DiscountService.disable()`

Sets `status = DiscountStatus.INACTIVE`. The `EXPIRED` status is defined in the enum but never set programmatically — it must be set manually via `PUT /discounts/{id}` or by an external scheduler (not implemented).

---

## 6. Inter-Service Communication

No inter-service HTTP communication is implemented. `productId` and `storeId` values are externally-provided `Long` IDs with no runtime validation against `product-service` or `store-service`.

---

## 7. Kafka and Asynchronous Events

Kafka is not implemented. The `build.gradle.kts` file contains no Kafka dependency.

---

## 8. Error Handling

A `GlobalExceptionHandler` (`@RestControllerAdvice`) is registered and handles all exceptions with a consistent JSON response body.

**HTTP status codes in use:**

| Scenario | Status |
|---|---|
| Successful GET / PUT / batch POST | `200 OK` |
| Successful POST (create) | `201 Created` |
| Successful DELETE (disable) | `204 No Content` |
| Bean Validation failure (`@Valid`) | `400 Bad Request` |
| Price or discount not found | `404 Not Found` |
| No active price for calculate | `404 Not Found` |
| Exchange rate not found | `404 Not Found` |
| Unhandled exception | `500 Internal Server Error` |

**Error response format:**
```json
{
  "status": 404,
  "error": "No price found for product 999"
}
```

**Validation error response format (`400`):**
```json
{
  "status": 400,
  "error": "Validation failed",
  "details": [
    "items: must not be empty"
  ]
}
```

---

## 9. Configuration and Deployment

### 9.1 Application Settings

| Env variable | Default | Description |
|---|---|---|
| `PRICING_SERVER_PORT` | `8084` | HTTP port the service listens on |
| `PRICING_API_PREFIX` | `/api` | Servlet context path |
| `PRICING_POSTGRES_HOST` | `localhost` | PostgreSQL hostname |
| `PRICING_POSTGRES_PORT` | `5432` | PostgreSQL port |
| `PRICING_POSTGRES_DB` | _(required)_ | Database name |
| `PRICING_POSTGRES_USER` | _(required)_ | Database username |
| `PRICING_POSTGRES_PASSWORD` | _(required)_ | Database password |
| `PRICING_HIKARI_POOL_MAX` | `25` | HikariCP maximum pool size |
| `PRICING_HIKARI_POOL_MIN` | `2` | HikariCP minimum idle connections |
| `PRICING_HIKARI_POOL_TIMEOUT_MILLIS` | `30000` | HikariCP connection timeout (ms) |
| `PRICING_POSTGRES_SHOW_SQL` | `true` | Log Hibernate SQL statements |
| `PRICING_POSTGRES_DDL_AUTO` | `update` | Hibernate DDL mode |
| `PRICING_SEED_ENABLE` | `false` | Enable data seeding on startup |
| `PRICING_SEED_MODE` | `init` | Seed data directory under `static/data/` |
| `PRICING_SEED_STOP_COUNT` | `1` | Skip seed if table already has more rows |

SSL for the JDBC connection is hardcoded to `disable` in `PostgresConfig`.

### 9.2 OpenAPI / Swagger UI

| Endpoint | URL |
|---|---|
| OpenAPI JSON spec | `http://localhost:8084/api/v3/api-docs` |
| Swagger UI | `http://localhost:8084/api/swagger-ui.html` |

### 9.3 Security

Spring Security is included but configured to **permit all requests**. CSRF is disabled. Session management is stateless.

### 9.4 Data Seeding

Three `CommandLineRunner` beans seed initial data from JSON files in `src/main/resources/static/data/{mode}/`:

| Order | Bean | File | Table |
|---|---|---|---|
| 1 | `ExchangeRateDataSeed` | `exchange_rate.json` | `exchange_rates` |
| 2 | `PriceDataSeed` | `price.json` | `prices` |
| 3 | `DiscountDataSeed` | `discount.json` | `discounts` |

Each runner skips if `PRICING_SEED_ENABLE=false` or if the table row count exceeds `PRICING_SEED_STOP_COUNT`.

### 9.5 Docker

**Dockerfile** (multi-stage build):
- Stage 1: `gradle:8.10-jdk21` — runs `./gradlew :pricing-service:bootJar --no-daemon -x test`
- Stage 2: `eclipse-temurin:21-jre-alpine` — copies the built JAR, exposes port `8084`

### 9.6 Running Locally

```bash
docker compose up postgres-pricing -d
./gradlew :pricing-service:bootRun
```

---

## 10. Testing

No test files exist under `src/test/`. The `spring-boot-starter-test` dependency is declared but no test classes have been written.

---

## 11. Known Limitations and Missing Functionality

### ✅ IMPLEMENTED

- CRUD for prices with active-price filtering and time-bound validity
- Automatic `PriceHistory` recording when price `amount` changes
- CRUD for discounts with soft-delete (status → `INACTIVE`)
- Single-item price calculation with `PERCENTAGE` and `FIXED` discount types
- **Batch price retrieval** (`POST /prices/batch`) — up to 100 product IDs, single `IN` query, validation
- **Batch price calculation** (`POST /prices/calculate/batch`) — up to 100 items, N+1-free, with optional currency conversion
- **Currency conversion in batch calculate** via `ExchangeRateService.getRate()`, exchange rates prefetched and cached per unique pair
- **PostgreSQL indexes** on `prices` (product_id, store_id, currency_code, valid_to) and `discounts` (store_id, product_id, status, starts_at)
- **Bean Validation** (`@NotEmpty`, `@Size`, `@Positive`, `@NotBlank`) on `BatchPriceRequest` and `BatchCalculateRequest`
- **Global exception handler** (`GlobalExceptionHandler`) with structured JSON error responses including validation details
- Exchange rate storage and retrieval (read-only via REST)
- Data seeding from bundled JSON files
- Swagger UI / OpenAPI spec endpoint
- HikariCP connection pool with configurable limits

### ⚠️ PARTIALLY IMPLEMENTED

- **Discount `EXPIRED` status:** The `EXPIRED` enum value is defined but never set programmatically. Expiry detection must be done via `PUT /discounts/{id}` with `status: "EXPIRED"` or inferred from `endsAt`.
- **`GET /discounts` filtering:** Returns all discounts via `findAll()`. No `storeId` or `productId` query parameters are exposed.
- **`POST /prices/calculate` currency conversion:** The single-item calculate endpoint requires `currencyCode` to match a stored price's currency exactly. Use `POST /prices/calculate/batch` for conversion.

### ❌ NOT IMPLEMENTED

- **No authentication or authorization:** Spring Security is configured to permit all requests.
- **No Kafka event publishing:** Price updates, discount creation, and price calculations do not emit any events.
- **No inter-service HTTP clients:** Product and store existence is not validated.
- **No exchange-rate auto-refresh:** Exchange rates are static seed data.
- **No `DELETE` endpoint for prices:** Prices can only be expired by setting `validTo` via `PUT /prices/{id}`.
- **No `GET /discounts/{id}` endpoint:** Individual discount lookup exists in `DiscountService` but is not exposed.
- **No tests:** Zero test classes exist.
- **No pagination on list endpoints:** `GET /discounts` and `GET /exchange-rates` return unbounded results.
