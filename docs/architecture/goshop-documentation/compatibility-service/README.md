# Compatibility Service

Part of the **GoShop** monorepo. Responsible for checking hardware component compatibility based on configurable rules (CPU socket, RAM type, PSU wattage, GPU clearance, cooling TDP, form factor).

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

**Purpose:** The Compatibility Service determines whether a set of hardware products (identified by UUIDs from the product-service) are mutually compatible, based on a library of typed compatibility rules stored in its own database.

**Responsibilities:**
- ✅ Storing and managing compatibility rules (`CompatibilityRule`)
- ✅ Running a multi-product compatibility check against all enabled rules and persisting the result (`CompatibilityCheck` + `CompatibilityIssue`)
- ✅ Running a point-to-point (pair) compatibility check as a convenience wrapper
- ❌ Returning a list of products compatible with a given product (stub — always returns an empty list)

**Data ownership:** The service owns its own PostgreSQL database (`compatibility_db`). References to product-type UUIDs from the product-service are stored as plain `UUID` columns — there are no cross-service foreign-key constraints.

---

## 2. Technology Stack

| Component | Version |
|---|---|
| Kotlin | 2.0.0 |
| JVM toolchain | 21 |
| Spring Boot | 3.3.5 |
| Spring Dependency Management plugin | 1.1.6 |

**Dependencies from `build.gradle.kts`:**

| Dependency | Version | Scope |
|---|---|---|
| `spring-boot-starter-web` | managed by Spring Boot 3.3.5 | implementation |
| `spring-boot-starter-data-jpa` | managed by Spring Boot 3.3.5 | implementation |
| `spring-boot-starter-security` | managed by Spring Boot 3.3.5 | implementation |
| `jackson-module-kotlin` | managed by Spring Boot 3.3.5 | implementation |
| `springdoc-openapi-starter-webmvc-ui` | 2.6.0 | implementation |
| `postgresql` (JDBC driver) | managed by Spring Boot 3.3.5 | runtimeOnly |
| `spring-boot-starter-test` | managed by Spring Boot 3.3.5 | testImplementation |

HikariCP is used as the connection pool (bundled with Spring Boot's JPA starter). The JPA provider is Hibernate with `PostgreSQLDialect`.

---

## 3. Database Model

### 3.1 Entity Relationship Overview

```
compatibility_rules  ←── compatibility_issues ───→  compatibility_checks
                                                          │
                                              compatibility_check_products
                                              (ElementCollection, one row per UUID)
```

### 3.2 `compatibility_rules`

Stores the library of typed rules. Each rule references two product-type UUIDs (cross-service references, no FK constraint).

| Column | Kotlin type | Nullable | Constraints | Notes |
|---|---|---|---|---|
| `id` | `UUID` | NO | PK, `GenerationType.UUID` | Auto-generated |
| `code` | `String` | NO | UNIQUE | Human-readable rule identifier, e.g. `CPU_MOTHERBOARD_SOCKET` |
| `source_product_type_id` | `UUID` | NO | — | Product-type UUID from product-service (source side) |
| `target_product_type_id` | `UUID` | NO | — | Product-type UUID from product-service (target side) |
| `rule_type` | `RuleType` (enum, stored as `STRING`) | NO | — | See enum values below |
| `parameters` | `String?` | YES | `columnDefinition = "jsonb"` | JSON parameters for the rule handler |
| `severity` | `Severity` (enum, stored as `STRING`) | NO | default `ERROR` | Outcome severity if the rule is violated |
| `enabled` | `Boolean` | NO | default `true` | Whether this rule participates in checks |
| `version` | `Int` | NO | default `1` | Incremented on each update/disable |

**Enum `RuleType`:** `SOCKET_MATCH`, `RAM_COMPATIBILITY`, `POWER_CAPACITY`, `GPU_CLEARANCE`, `COOLING_TDP`, `FORM_FACTOR`

**Enum `Severity`:** `ERROR`, `WARNING`, `INFO`

### 3.3 `compatibility_checks`

One record per invocation of the compatibility check endpoint.

| Column | Kotlin type | Nullable | Constraints | Notes |
|---|---|---|---|---|
| `id` | `UUID` | NO | PK, `GenerationType.UUID` | Auto-generated |
| `compatible` | `Boolean` | NO | — | `true` only when no `ERROR`-severity issues were found |
| `checked_at` | `Instant` | NO | `updatable = false`, default `Instant.now()` | Timestamp of the check |

### 3.4 `compatibility_check_products` (`@ElementCollection`)

Join table backing the `productIds` collection on `CompatibilityCheck`.

| Column | Type | Notes |
|---|---|---|
| `check_id` | `UUID` | FK → `compatibility_checks.id` |
| `product_id` | `UUID` | UUID of a product from product-service |

### 3.5 `compatibility_issues`

One record per rule violation found during a check.

| Column | Kotlin type | Nullable | Constraints | Notes |
|---|---|---|---|---|
| `id` | `UUID` | NO | PK, `GenerationType.UUID` | Auto-generated |
| `check_id` | `UUID` | NO | FK → `compatibility_checks.id` (LAZY `@ManyToOne`) | Parent check |
| `rule_id` | `UUID` | NO | FK → `compatibility_rules.id` (LAZY `@ManyToOne`) | Rule that produced the issue |
| `severity` | `Severity` (enum, stored as `STRING`) | NO | — | Copied from the rule at the time of the check |
| `message_code` | `String` | NO | — | i18n key for the issue message, e.g. `SOCKET_MISMATCH` |

---

## 4. REST API Endpoints

Base URL: `http://localhost:8082/api`

OpenAPI UI: `http://localhost:8082/api/swagger-ui.html`

OpenAPI JSON: `http://localhost:8082/api/v3/api-docs`

Security: all requests are permitted without authentication (CSRF disabled, stateless session).

---

### 4.1 `POST /api/compatibility/check`

**Controller:** `CompatibilityController` — `check()`

**Purpose:** Run a compatibility check against all enabled rules for a list of product UUIDs. Persists the check and all detected issues. Returns the aggregate result.

**Request body — `CheckRequest`:**

| Field | Type | Required | Notes |
|---|---|---|---|
| `productIds` | `List<UUID>` | YES | UUIDs of the products to check together |

**Response body — `CheckResponse`:**

| Field | Type | Notes |
|---|---|---|
| `compatible` | `Boolean` | `true` when no `ERROR`-severity issues were found |
| `issues` | `List<CompatibilityIssueResponse>` | All violated rules (may be empty) |

**`CompatibilityIssueResponse`:**

| Field | Type | Notes |
|---|---|---|
| `ruleId` | `UUID` | ID of the violated rule |
| `severity` | `Severity` | `ERROR`, `WARNING`, or `INFO` |
| `messageCode` | `String` | i18n key for the issue |

**HTTP status codes:** `200 OK`

**Example request:**
```json
POST /api/compatibility/check
Content-Type: application/json

{
  "productIds": [
    "a1b2c3d4-0001-0000-0000-000000000001",
    "a1b2c3d4-0002-0000-0000-000000000002",
    "a1b2c3d4-0003-0000-0000-000000000003"
  ]
}
```

**Example response (compatible):**
```json
{
  "compatible": true,
  "issues": []
}
```

**Example response (incompatible):**
```json
{
  "compatible": false,
  "issues": [
    {
      "ruleId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
      "severity": "ERROR",
      "messageCode": "SOCKET_MISMATCH"
    }
  ]
}
```

**Possible errors:** None explicitly thrown by this endpoint. Any rule evaluation exception is silently swallowed via `runCatching { }.getOrNull()`.

---

### 4.2 `POST /api/compatibility/pairs/check`

**Controller:** `CompatibilityController` — `checkPair()`

**Purpose:** Convenience wrapper for checking exactly two products against each other. Internally delegates to `check()` with a two-element list.

**Request body — `PairCheckRequest`:**

| Field | Type | Required | Notes |
|---|---|---|---|
| `sourceProductId` | `UUID` | YES | First product UUID |
| `targetProductId` | `UUID` | YES | Second product UUID |

**Response body:** Same as `CheckResponse` above.

**HTTP status codes:** `200 OK`

**Example request:**
```json
POST /api/compatibility/pairs/check
Content-Type: application/json

{
  "sourceProductId": "a1b2c3d4-0001-0000-0000-000000000001",
  "targetProductId": "a1b2c3d4-0002-0000-0000-000000000002"
}
```

**Example response:**
```json
{
  "compatible": true,
  "issues": []
}
```

---

### 4.3 `GET /api/compatibility/products/{id}`

**Controller:** `CompatibilityController` — `findCompatible()`

**Purpose:** Return a list of product UUIDs that are compatible with the given product.

**Path variables:**

| Variable | Type | Notes |
|---|---|---|
| `id` | `UUID` | UUID of the reference product |

**Response body:** `List<UUID>`

**HTTP status codes:** `200 OK`

**Example request:**
```
GET /api/compatibility/products/a1b2c3d4-0001-0000-0000-000000000001
```

**Example response:**
```json
[]
```

**Note:** ⚠️ This endpoint always returns an empty list. See [Known Limitations](#11-known-limitations-and-missing-functionality).

---

### 4.4 `GET /api/compatibility/rules`

**Controller:** `CompatibilityRuleController` — `getAll()`

**Purpose:** Return all compatibility rules (enabled and disabled).

**Response body:** `List<CompatibilityRuleResponse>`

**`CompatibilityRuleResponse`:**

| Field | Type | Notes |
|---|---|---|
| `id` | `UUID` | Rule ID |
| `code` | `String` | Unique rule code |
| `sourceProductTypeId` | `UUID` | Source product-type UUID |
| `targetProductTypeId` | `UUID` | Target product-type UUID |
| `ruleType` | `RuleType` | One of the six rule types |
| `parameters` | `String?` | JSON parameters, may be `null` |
| `severity` | `Severity` | `ERROR`, `WARNING`, or `INFO` |
| `enabled` | `Boolean` | Whether the rule is active |
| `version` | `Int` | Rule version number |

**HTTP status codes:** `200 OK`

**Example response:**
```json
[
  {
    "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
    "code": "CPU_MOTHERBOARD_SOCKET",
    "sourceProductTypeId": "22222222-2222-2222-2222-222222222001",
    "targetProductTypeId": "22222222-2222-2222-2222-222222222002",
    "ruleType": "SOCKET_MATCH",
    "parameters": "{\"cpu_field\": \"socket\", \"motherboard_field\": \"socket\"}",
    "severity": "ERROR",
    "enabled": true,
    "version": 1
  }
]
```

---

### 4.5 `POST /api/compatibility/rules`

**Controller:** `CompatibilityRuleController` — `create()`

**Purpose:** Create a new compatibility rule.

**Request body — `CompatibilityRuleCreateRequest`:**

| Field | Type | Required | Default | Notes |
|---|---|---|---|---|
| `code` | `String` | YES | — | Must be unique across all rules |
| `sourceProductTypeId` | `UUID` | YES | — | Product-type UUID (source) |
| `targetProductTypeId` | `UUID` | YES | — | Product-type UUID (target) |
| `ruleType` | `RuleType` | YES | — | `SOCKET_MATCH`, `RAM_COMPATIBILITY`, `POWER_CAPACITY`, `GPU_CLEARANCE`, `COOLING_TDP`, or `FORM_FACTOR` |
| `parameters` | `String?` | NO | `null` | JSON string with handler parameters |
| `severity` | `Severity` | NO | `ERROR` | `ERROR`, `WARNING`, or `INFO` |
| `enabled` | `Boolean` | NO | `true` | Whether the rule is active immediately |

**Response body:** `CompatibilityRuleResponse` (see 4.4)

**HTTP status codes:** `201 Created`, `409 Conflict` (duplicate `code`)

**Example request:**
```json
POST /api/compatibility/rules
Content-Type: application/json

{
  "code": "CPU_MOTHERBOARD_SOCKET",
  "sourceProductTypeId": "22222222-2222-2222-2222-222222222001",
  "targetProductTypeId": "22222222-2222-2222-2222-222222222002",
  "ruleType": "SOCKET_MATCH",
  "parameters": "{\"cpu_field\": \"socket\", \"motherboard_field\": \"socket\"}",
  "severity": "ERROR",
  "enabled": true
}
```

**Example response:**
```json
{
  "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "code": "CPU_MOTHERBOARD_SOCKET",
  "sourceProductTypeId": "22222222-2222-2222-2222-222222222001",
  "targetProductTypeId": "22222222-2222-2222-2222-222222222002",
  "ruleType": "SOCKET_MATCH",
  "parameters": "{\"cpu_field\": \"socket\", \"motherboard_field\": \"socket\"}",
  "severity": "ERROR",
  "enabled": true,
  "version": 1
}
```

**Possible errors:**
- `409 Conflict` — a rule with the same `code` already exists

---

### 4.6 `PUT /api/compatibility/rules/{id}`

**Controller:** `CompatibilityRuleController` — `update()`

**Purpose:** Partially update a rule. Only `parameters`, `severity`, and `enabled` can be changed. `code`, `sourceProductTypeId`, `targetProductTypeId`, and `ruleType` are immutable. Each successful update increments `version` by 1.

**Path variables:**

| Variable | Type | Notes |
|---|---|---|
| `id` | `UUID` | Rule ID |

**Request body — `CompatibilityRuleUpdateRequest`:**

| Field | Type | Required | Notes |
|---|---|---|---|
| `parameters` | `String?` | NO | If `null`, existing value is kept |
| `severity` | `Severity?` | NO | If `null`, existing value is kept |
| `enabled` | `Boolean?` | NO | If `null`, existing value is kept |

**Response body:** `CompatibilityRuleResponse` (see 4.4)

**HTTP status codes:** `200 OK`, `404 Not Found`

**Example request:**
```json
PUT /api/compatibility/rules/f47ac10b-58cc-4372-a567-0e02b2c3d479
Content-Type: application/json

{
  "severity": "WARNING",
  "enabled": false
}
```

**Example response:**
```json
{
  "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "code": "CPU_MOTHERBOARD_SOCKET",
  "sourceProductTypeId": "22222222-2222-2222-2222-222222222001",
  "targetProductTypeId": "22222222-2222-2222-2222-222222222002",
  "ruleType": "SOCKET_MATCH",
  "parameters": "{\"cpu_field\": \"socket\", \"motherboard_field\": \"socket\"}",
  "severity": "WARNING",
  "enabled": false,
  "version": 2
}
```

**Possible errors:**
- `404 Not Found` — rule with given ID does not exist

---

### 4.7 `DELETE /api/compatibility/rules/{id}`

**Controller:** `CompatibilityRuleController` — `disable()`

**Purpose:** Disable a rule (sets `enabled = false` and increments `version`). The rule record is **not** deleted from the database.

**Path variables:**

| Variable | Type | Notes |
|---|---|---|
| `id` | `UUID` | Rule ID |

**Response body:** none

**HTTP status codes:** `204 No Content`, `404 Not Found`

**Example request:**
```
DELETE /api/compatibility/rules/f47ac10b-58cc-4372-a567-0e02b2c3d479
```

**Possible errors:**
- `404 Not Found` — rule with given ID does not exist

---

## 5. Business Logic

### 5.1 `CompatibilityCheckService`

#### `check(dto: CheckCompatibilityDto): CompatibilityResult`

Runs in a single `@Transactional` block:

1. Loads all enabled rules via `CompatibilityRuleRepository.findByEnabledTrue()`.
2. Saves a new `CompatibilityCheck` with `compatible = true` and the provided `productIds`.
3. For each rule, calls `evaluateRule(rule.id, dto.productIds)` inside `runCatching {}`. If the call returns a non-null `messageCode`, a `CompatibilityIssue` is persisted for that rule.
4. If any saved issue has `severity == ERROR`, re-saves the check with `compatible = false`.
5. Returns `CompatibilityResult(compatible = !hasErrors, issues = <saved issues>)`.

**`CompatibilityResult`** (internal data class, not a JPA entity):

| Field | Type | Notes |
|---|---|---|
| `compatible` | `Boolean` | `true` only when no ERROR-severity issues found |
| `issues` | `List<CompatibilityIssue>` | Persisted issue entities |

**Important:** `evaluateRule()` is a stub that always returns `null` (no violation). This means every check currently returns `compatible = true` with zero issues regardless of the actual products. See [Known Limitations](#11-known-limitations-and-missing-functionality).

#### `checkPair(dto: PairCheckDto): CompatibilityResult`

Convenience method. Constructs a `CheckCompatibilityDto` with `listOf(dto.sourceProductId, dto.targetProductId)` and delegates to `check()`.

#### `findCompatibleProducts(productId: UUID): List<UUID>`

Stub method. Always returns `emptyList()`. Comment in source: "full implementation requires product catalog integration."

---

### 5.2 `CompatibilityRuleService`

#### `findAll(): List<CompatibilityRule>`

Returns all rules via `JpaRepository.findAll()`. No filtering.

#### `findById(id: UUID): CompatibilityRule`

Looks up a rule by ID. Throws `ResponseStatusException(404)` if not found.

#### `create(dto: CreateCompatibilityRuleDto): CompatibilityRule`

1. Checks `existsByCode(dto.code)`. If the code already exists, throws `ResponseStatusException(409)`.
2. Saves a new `CompatibilityRule` with `version = 1` (default).

#### `update(id: UUID, dto: UpdateCompatibilityRuleDto): CompatibilityRule`

1. Loads the existing rule (throws 404 if absent).
2. Saves a new `CompatibilityRule` entity instance with the same `id` and immutable fields (`code`, `sourceProductTypeId`, `targetProductTypeId`, `ruleType`), merging the nullable patch fields from the DTO: if `dto.parameters` is `null`, keeps `existing.parameters`; same for `severity` and `enabled`.
3. Increments `version` by 1.

#### `disable(id: UUID)`

1. Loads the existing rule (throws 404 if absent).
2. Saves a new entity instance with `enabled = false` and `version + 1`. Does not delete the record.

---

## 6. Inter-Service Communication

No inter-service communication is currently implemented.

Product-type UUIDs in `CompatibilityRule` and product UUIDs in `CompatibilityCheck` are plain identifiers — the service does not make any HTTP calls to the product-service or any other service.

---

## 7. Kafka and Asynchronous Events

Kafka is not implemented. There is no Kafka dependency in `build.gradle.kts` and no event publishers or consumers in the source code.

---

## 8. Error Handling

There is no `@ControllerAdvice` or `@ExceptionHandler` in the codebase. Error responses are produced entirely by Spring Boot's default `BasicErrorController`.

Errors thrown explicitly:

| Location | Exception | HTTP Status | Message |
|---|---|---|---|
| `CompatibilityRuleService.findById` | `ResponseStatusException` | `404 Not Found` | `"Rule {id} not found"` |
| `CompatibilityRuleService.create` | `ResponseStatusException` | `409 Conflict` | `"Rule with code '{code}' already exists"` |

**Default Spring Boot error response format:**

```json
{
  "timestamp": "2026-11-06T12:00:00.000+00:00",
  "status": 404,
  "error": "Not Found",
  "path": "/api/compatibility/rules/00000000-0000-0000-0000-000000000000"
}
```

Rule evaluation exceptions inside `check()` are silently swallowed by `runCatching { }.getOrNull()` — they do not surface as HTTP errors.

---

## 9. Configuration and Deployment

### Port and Context Path

| Setting | Value |
|---|---|
| HTTP port | `8082` |
| Context path | `/api` |
| All endpoints are therefore prefixed | `/api/...` |

### Environment Variables

| Variable | Default | Required | Description |
|---|---|---|---|
| `COMPATIBILITY_SERVER_PORT` | `8082` | NO | HTTP server port |
| `COMPATIBILITY_API_PREFIX` | `/api` | NO | Servlet context path |
| `COMPATIBILITY_POSTGRES_HOST` | `localhost` | NO | PostgreSQL host |
| `COMPATIBILITY_POSTGRES_PORT` | `5432` | NO | PostgreSQL port |
| `COMPATIBILITY_POSTGRES_DB` | — | **YES** | Database name (no default) |
| `COMPATIBILITY_POSTGRES_USER` | — | **YES** | Database user (no default) |
| `COMPATIBILITY_POSTGRES_PASSWORD` | — | **YES** | Database password (no default) |
| `COMPATIBILITY_HIKARI_POOL_MAX` | `25` | NO | HikariCP maximum pool size |
| `COMPATIBILITY_HIKARI_POOL_MIN` | `2` | NO | HikariCP minimum idle connections |
| `COMPATIBILITY_HIKARI_POOL_TIMEOUT_MILLIS` | `30000` | NO | HikariCP connection timeout (ms) |
| `COMPATIBILITY_POSTGRES_SHOW_SQL` | `true` | NO | Log SQL statements |
| `COMPATIBILITY_POSTGRES_DDL_AUTO` | `update` | NO | Hibernate DDL mode (`update`, `validate`, `none`) |
| `COMPATIBILITY_SEED_ENABLE` | `false` | NO | Run data seeder on startup |
| `COMPATIBILITY_SEED_MODE` | `init` | NO | Seed dataset subdirectory under `static/data/` |
| `COMPATIBILITY_SEED_STOP_COUNT` | `1` | NO | Skip seeding if rule count exceeds this value |

### Data Seeder

When `COMPATIBILITY_SEED_ENABLE=true`, `CompatibilityRuleDataSeed` runs at startup (order 1). It reads `src/main/resources/static/data/{COMPATIBILITY_SEED_MODE}/compatibility_rule.json` and inserts rows into `compatibility_rules`. Seeding is skipped if the existing row count exceeds `COMPATIBILITY_SEED_STOP_COUNT`.

The bundled `init` dataset contains five rules:

| Code | Rule Type | Severity |
|---|---|---|
| `CPU_MOTHERBOARD_SOCKET` | `SOCKET_MATCH` | `ERROR` |
| `RAM_MOTHERBOARD_DDR` | `RAM_COMPATIBILITY` | `ERROR` |
| `PSU_SYSTEM_POWER` | `POWER_CAPACITY` | `WARNING` |
| `GPU_CASE_CLEARANCE` | `GPU_CLEARANCE` | `ERROR` |
| `CPU_COOLER_TDP` | `COOLING_TDP` | `WARNING` |

### Dockerfile

Multi-stage build:
1. **Builder stage** — `gradle:8.10-jdk21`: runs `./gradlew :compatibility-service:bootJar --no-daemon -x test`
2. **Runtime stage** — `eclipse-temurin:21-jre-alpine`: copies the JAR, exposes port `8082`

### docker-compose.yml Entry

```yaml
compatibility-service:
  build:
    context: .
    dockerfile: compatibility-service/Dockerfile
  container_name: compatibility-service
  ports:
    - "8082:8082"
  environment:
    COMPATIBILITY_POSTGRES_HOST: postgres-compatibility
    COMPATIBILITY_POSTGRES_PORT: 5432
    COMPATIBILITY_POSTGRES_DB: compatibility_db
    COMPATIBILITY_POSTGRES_USER: postgres
    COMPATIBILITY_POSTGRES_PASSWORD: postgres
    COMPATIBILITY_POSTGRES_DDL_AUTO: update
    COMPATIBILITY_POSTGRES_SHOW_SQL: "false"
    COMPATIBILITY_SEED_ENABLE: "false"
  depends_on:
    postgres-compatibility:
      condition: service_healthy
  networks:
    - goshop-network
```

The PostgreSQL container for this service is `postgres-compatibility` (image `postgres:16-alpine`), mapped to host port `5433`.

### Local Run

```bash
# 1. Start the database
docker compose up postgres-compatibility -d

# 2. Copy and fill in env vars
cp compatibility-service/.env.sample .env

# 3. Run the service
./gradlew :compatibility-service:bootRun
```

---

## 10. Testing

No test classes exist under `src/test/`. The `spring-boot-starter-test` dependency is declared in `build.gradle.kts` and JUnit Platform is configured, but no test files have been written.

---

## 11. Known Limitations and Missing Functionality

### ❌ Rule evaluation is a stub

`CompatibilityCheckService.evaluateRule(ruleId, productIds)` always returns `null`, meaning no rule is ever considered violated. Every compatibility check returns `compatible = true` with an empty issue list regardless of the products provided.

The six `RuleType` values (`SOCKET_MATCH`, `RAM_COMPATIBILITY`, `POWER_CAPACITY`, `GPU_CLEARANCE`, `COOLING_TDP`, `FORM_FACTOR`) are defined but have no corresponding handler implementations.

### ❌ `findCompatibleProducts` is a stub

`GET /api/compatibility/products/{id}` always returns `[]`. The source code comment states: "full implementation requires product catalog integration."

### ❌ No cross-service product validation

The service stores product UUIDs and product-type UUIDs as plain identifiers. It does not validate that the referenced products or product types actually exist in the product-service.

### ❌ No test coverage

There are zero test classes in the project.

### ⚠️ Silent rule evaluation errors

Inside `check()`, each call to `evaluateRule()` is wrapped in `runCatching { }.getOrNull()`. Any runtime exception from a rule handler is silently swallowed and treated as "no violation", making debugging failed rule evaluations difficult.

### ⚠️ Update replaces the entity by constructing a new instance

`CompatibilityRuleService.update()` and `disable()` construct a new `CompatibilityRule` with the same `id` rather than mutating the loaded entity. This is correct for JPA `save()` (merge semantics), but is an unconventional pattern that may be unexpected.

### ⚠️ `FORM_FACTOR` rule type has no seed data

The `FORM_FACTOR` enum value exists in `RuleType` but no corresponding seed entry is present in `data/init/compatibility_rule.json`.
