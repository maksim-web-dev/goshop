# Recommendations Service

Part of the **GoShop** e-commerce monorepo. Responsible for surfacing similar, alternative, accessory, and upgrade product recommendations based on statically curated relation data. No machine-learning or real-time scoring engine is present — all recommendations are resolved from pre-populated `product_relations` rows and cached in `recommendation_snapshots`.

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

| Property | Value |
|---|---|
| Service name | `recommendations-service` |
| Spring application name | `recommendations-service` |
| Default port | `8085` |
| Context path | `/api` |
| Database | PostgreSQL (dedicated instance `recommendations_db`) |

**Responsibilities:**

- Store and query directional product relations (`SIMILAR`, `ALTERNATIVE`, `ACCESSORY`, `UPGRADE`) with a relevance score.
- Expose per-product recommendation endpoints filtered by relation type, ordered by score descending.
- Provide a bulk "build for PC" endpoint that resolves all relations for a list of product IDs in a single call.
- Manage `RecommendationRule` entities that describe named strategies with optional JSONB parameters; rules drive future algorithmic expansion.
- Generate and upsert `RecommendationSnapshot` records that pre-compute and persist recommended product ID lists for a given (product, strategy) pair.

**Data ownership:**

The service owns all three tables: `recommendation_rules`, `product_relations`, and `recommendation_snapshots` / `recommendation_snapshot_products`. Product IDs from other services are stored as plain `Long` values (cross-service references) — no foreign keys to other databases.

---

## 2. Technology Stack

| Component | Version / Artifact |
|---|---|
| Kotlin | 2.0.0 |
| JVM toolchain | 21 |
| Spring Boot | 3.3.5 |
| Spring Dependency Management plugin | 1.1.6 |
| Kotlin plugin for Spring | 2.0.0 |
| Kotlin plugin for JPA | 2.0.0 |
| `spring-boot-starter-web` | (managed by Spring Boot 3.3.5) |
| `spring-boot-starter-data-jpa` | (managed) |
| `spring-boot-starter-security` | (managed) |
| `spring-boot-starter-validation` | (managed) |
| `jackson-module-kotlin` | (managed) |
| `springdoc-openapi-starter-webmvc-ui` | 2.6.0 |
| `org.postgresql:postgresql` | (managed, runtime) |
| `spring-boot-starter-test` | (managed, test scope) |
| Gradle wrapper | 8.10 |
| Docker base (build stage) | `gradle:8.10-jdk21` |
| Docker base (runtime stage) | `eclipse-temurin:21-jre-alpine` |

No Kafka, Redis, or external HTTP client dependencies are present in `build.gradle.kts`.

---

## 3. Database Model

### 3.1 `RecommendationRule` — table `recommendation_rules`

Stores named recommendation strategies with optional JSONB configuration parameters.

| Column | Kotlin type | Nullable | Notes |
|---|---|---|---|
| `id` | `Long` | No | PK, `GenerationType.IDENTITY` |
| `name` | `String` | No | Human-readable rule name |
| `strategy` | `RecommendationStrategy` | No | Stored as `STRING` enum |
| `parameters` | `String?` | Yes | `columnDefinition = "jsonb"` — strategy-specific config |
| `priority` | `Int` | No | Higher value = higher priority; default `0` |
| `enabled` | `Boolean` | No | Whether the rule is active; default `true` |

Repository method: `findByEnabledTrueOrderByPriorityDesc(): List<RecommendationRule>` — used to list active rules sorted by priority (not currently called by any service layer logic).

### 3.2 `ProductRelation` — table `product_relations`

Stores directional, typed product relationships with a relevance score.

| Column | Kotlin type | Nullable | Notes |
|---|---|---|---|
| `id` | `Long` | No | PK, `GenerationType.IDENTITY` |
| `source_product_id` | `Long` | No | The product being viewed |
| `target_product_id` | `Long` | No | The recommended product |
| `relation_type` | `RelationType` | No | Stored as `STRING` enum |
| `score` | `Double` | No | Relevance weight in range `0.0`–`1.0`; default `0.0` |

**Unique constraint:** `(source_product_id, target_product_id, relation_type)` — a given source/target pair may appear at most once per relation type.

**Indexes on `product_relations`:**

| Index name | Columns | Purpose |
|---|---|---|
| `idx_product_relations_source_id` | `source_product_id` | Lookup by source product |
| `idx_product_relations_source_type` | `source_product_id, relation_type` | Filtered lookup by source + type |

**`RelationType` enum values:**

| Value | Meaning |
|---|---|
| `SIMILAR` | Product with comparable attributes |
| `ALTERNATIVE` | Drop-in substitute |
| `ACCESSORY` | Complementary add-on |
| `UPGRADE` | Higher-tier replacement |

Repository methods:
- `findBySourceProductIdAndRelationTypeOrderByScoreDesc(sourceProductId, relationType)` — filtered by type, ordered by score
- `findBySourceProductIdOrderByScoreDesc(sourceProductId)` — all types, ordered by score
- `findBySourceProductIdIn(ids)` — batch lookup for multiple source IDs in a single query; used by `buildForPc`

### 3.3 `RecommendationSnapshot` — tables `recommendation_snapshots` + `recommendation_snapshot_products`

Pre-computed snapshot of recommended product IDs for a (product, strategy) pair.

| Column | Kotlin type | Nullable | Notes |
|---|---|---|---|
| `id` | `Long` | No | PK, `GenerationType.IDENTITY` |
| `product_id` | `Long` | No | Product for which the snapshot was generated |
| `strategy` | `RecommendationStrategy` | No | Strategy used to generate the list |
| `recommended_product_ids` | `MutableList<UUID>` | No | `@ElementCollection` stored in `recommendation_snapshot_products`; `@OrderColumn(name = "position")` preserves insertion order |
| `generated_at` | `Instant` | No | Timestamp set at object construction; `updatable = false` |

Child table `recommendation_snapshot_products`:

| Column | Type |
|---|---|
| `snapshot_id` | Long (FK to `recommendation_snapshots.id`) |
| `position` | INT (order column) |
| `recommended_product_id` | Long |

**`RecommendationStrategy` enum values:**

| Value | Intended meaning |
|---|---|
| `SIMILAR_ATTRIBUTES` | Products sharing key attribute values |
| `SAME_CATEGORY` | Products in the same category |
| `PRICE_RANGE` | Products within a comparable price bracket |
| `COMPATIBILITY_BASED` | Products verified as hardware-compatible |
| `POPULAR_IN_CATEGORY` | Trending products in the same category |

**Indexes on `recommendation_snapshots`:**

| Index name | Columns | Purpose |
|---|---|---|
| `idx_recommendation_snapshots_product_id` | `product_id` | Lookup by product |
| `idx_recommendation_snapshots_product_strategy` | `product_id, strategy` | Exact match for upsert check |

Repository method: `findByProductIdAndStrategy(productId, strategy): RecommendationSnapshot?`

---

## 4. REST API Endpoints

Base URL: `http://localhost:8085/api`

Swagger UI: `http://localhost:8085/api/swagger-ui.html`

OpenAPI JSON: `http://localhost:8085/api/v3/api-docs`

Security is fully open — `SecurityConfig` disables CSRF, sets stateless sessions, and permits all requests.

---

### 4.1 `GET /recommendations/products/{id}`

| Property | Value |
|---|---|
| Controller | `RecommendationController` |
| Method | `getAll` |
| Purpose | Returns all recommended product IDs for the given source product, across all relation types, sorted by score descending |

**Path variables:**

| Name | Type | Required | Description |
|---|---|---|---|
| `id` | `Long` | Yes | Source product ID |

**Request body:** none

**Response:** `200 OK` — `List<Long>` (JSON array of integers)

**Example request:**
```
GET /api/recommendations/products/1001
```

**Example response:**
```json
[1006, 1007, 1008]
```

**Possible errors:**

| Status | Condition |
|---|---|
| `200` with empty array | No relations exist for the given product ID |

---

### 4.2 `GET /recommendations/products/{id}/similar`

| Property | Value |
|---|---|
| Controller | `RecommendationController` |
| Method | `getSimilar` |
| Purpose | Returns product IDs related to `{id}` with relation type `SIMILAR`, sorted by score descending |

**Path variables:**

| Name | Type | Required | Description |
|---|---|---|---|
| `id` | `Long` | Yes | Source product ID |

**Request body:** none

**Response:** `200 OK` — `List<Long>`

**Example request:**
```
GET /api/recommendations/products/1001/similar
```

**Example response:**
```json
[1006]
```

**Possible errors:** `200` with empty array if no `SIMILAR` relations exist.

---

### 4.3 `GET /recommendations/products/{id}/alternatives`

| Property | Value |
|---|---|
| Controller | `RecommendationController` |
| Method | `getAlternatives` |
| Purpose | Returns product IDs related to `{id}` with relation type `ALTERNATIVE`, sorted by score descending |

**Path variables:**

| Name | Type | Required | Description |
|---|---|---|---|
| `id` | `Long` | Yes | Source product ID |

**Request body:** none

**Response:** `200 OK` — `List<Long>`

**Example request:**
```
GET /api/recommendations/products/1001/alternatives
```

**Example response:**
```json
[1007]
```

---

### 4.4 `GET /recommendations/products/{id}/accessories`

| Property | Value |
|---|---|
| Controller | `RecommendationController` |
| Method | `getAccessories` |
| Purpose | Returns product IDs related to `{id}` with relation type `ACCESSORY`, sorted by score descending |

**Path variables:**

| Name | Type | Required | Description |
|---|---|---|---|
| `id` | `Long` | Yes | Source product ID |

**Request body:** none

**Response:** `200 OK` — `List<Long>`

**Example request:**
```
GET /api/recommendations/products/1001/accessories
```

**Example response:**
```json
[1008]
```

---

### 4.5 `POST /recommendations/products/build`

| Property | Value |
|---|---|
| Controller | `RecommendationController` |
| Method | `buildForPc` |
| Purpose | Bulk-resolves all recommendations for a list of product IDs; intended for PC-build use cases where multiple components are displayed simultaneously |

**Request body:** `BuildRecommendationRequest`

| Field | Type | Required | Constraints | Description |
|---|---|---|---|---|
| `productIds` | `List<Long>` | Yes | `@NotEmpty`, `@Size(max=100)` | Source product IDs |

**Response:** `200 OK` — `Map<Long, List<Long>>`

Each key is an input product ID; the value is the list of recommended product IDs (all relation types, sorted by score descending). Products with no relations are present in the map with an empty list.

**Example request:**
```json
{
  "productIds": [1001, 1002]
}
```

**Example response:**
```json
{
  "1001": [1006, 1007, 1008],
  "1002": [1003, 1009, 1010]
}
```

**N+1 prevention:** a single JPQL `IN` query (`findBySourceProductIdIn`) loads all relations for all product IDs at once; grouping is done in memory.

**Possible errors:**

| Status | Condition |
|---|---|
| `400 Bad Request` | `productIds` is empty or exceeds 100 items |

---

### 4.6 `GET /recommendations/rules`

| Property | Value |
|---|---|
| Controller | `RecommendationRuleController` |
| Method | `getAll` |
| Purpose | Returns all recommendation rules (enabled and disabled), without ordering |

**Request body:** none

**Response:** `200 OK` — `List<RecommendationRuleResponse>`

`RecommendationRuleResponse` fields:

| Field | Type | Nullable | Description |
|---|---|---|---|
| `id` | `Long` | No | Rule ID |
| `name` | `String` | No | Rule name |
| `strategy` | `RecommendationStrategy` | No | One of the five strategy enum values |
| `parameters` | `String?` | Yes | Raw JSONB string or `null` |
| `priority` | `Int` | No | Priority value |
| `enabled` | `Boolean` | No | Whether the rule is active |

**Example response:**
```json
[
  {
    "id": 1,
    "name": "Similar GPU by attributes",
    "strategy": "SIMILAR_ATTRIBUTES",
    "parameters": "{\"fields\": [\"memory_gb\", \"memory_type\", \"tdp_w\"], \"tolerance\": 0.2}",
    "priority": 10,
    "enabled": true
  }
]
```

---

### 4.7 `POST /recommendations/rules`

| Property | Value |
|---|---|
| Controller | `RecommendationRuleController` |
| Method | `create` |
| Purpose | Creates a new recommendation rule |

**Request body:** `RecommendationRuleCreateRequest`

| Field | Type | Required | Default | Constraints | Description |
|---|---|---|---|---|---|
| `name` | `String` | Yes | — | `@NotBlank`, `@Size(max=255)` | Human-readable name |
| `strategy` | `RecommendationStrategy` | Yes | — | Strategy enum value |
| `parameters` | `String?` | No | `null` | JSONB string with strategy-specific config |
| `priority` | `Int` | No | `0` | Rule priority |
| `enabled` | `Boolean` | No | `true` | Whether the rule is active |

**Response:** `201 Created` — `RecommendationRuleResponse` (same schema as section 4.6)

**Example request:**
```json
{
  "name": "Compatible accessories",
  "strategy": "COMPATIBILITY_BASED",
  "parameters": "{\"relation_types\": [\"ACCESSORY\"]}",
  "priority": 8,
  "enabled": true
}
```

**Example response:**
```json
{
  "id": 2,
  "name": "Compatible accessories",
  "strategy": "COMPATIBILITY_BASED",
  "parameters": "{\"relation_types\": [\"ACCESSORY\"]}",
  "priority": 8,
  "enabled": true
}
```

**Possible errors:**

| Status | Condition |
|---|---|
| `400 Bad Request` | Invalid `strategy` enum value or malformed JSON body |

---

### 4.8 `PUT /recommendations/rules/{id}`

| Property | Value |
|---|---|
| Controller | `RecommendationRuleController` |
| Method | `update` |
| Purpose | Partially updates a recommendation rule; `strategy` cannot be changed via this endpoint |

**Path variables:**

| Name | Type | Required | Description |
|---|---|---|---|
| `id` | `Long` | Yes | Rule ID |

**Request body:** `RecommendationRuleUpdateRequest` — all fields are optional

| Field | Type | Required | Description |
|---|---|---|---|
| `name` | `String?` | No | New name; if `null`, existing value is kept |
| `parameters` | `String?` | No | New JSONB parameters; if `null`, existing value is kept |
| `priority` | `Int?` | No | New priority; if `null`, existing value is kept |
| `enabled` | `Boolean?` | No | New enabled flag; if `null`, existing value is kept |

Note: `strategy` is not a field in `RecommendationRuleUpdateRequest` and **cannot** be changed after creation.

**Response:** `200 OK` — `RecommendationRuleResponse`

**Example request:**
```json
{
  "priority": 15,
  "enabled": false
}
```

**Example response:**
```json
{
  "id": 2,
  "name": "Compatible accessories",
  "strategy": "COMPATIBILITY_BASED",
  "parameters": "{\"relation_types\": [\"ACCESSORY\"]}",
  "priority": 15,
  "enabled": false
}
```

**Possible errors:**

| Status | Condition |
|---|---|
| `404 Not Found` | Rule with the given `id` does not exist |
| `400 Bad Request` | Malformed UUID path variable or invalid JSON body |

---

## 5. Business Logic

### `RecommendationService`

**`findAll(productId: Long): List<Long>`**

Calls `ProductRelationRepository.findBySourceProductIdOrderByScoreDesc`. Returns target product IDs for all relation types, ordered by score descending.

**`findSimilar` / `findAlternatives` / `findAccessories`**

Each delegates to the private `findByType(productId, type)` helper, which calls `findBySourceProductIdAndRelationTypeOrderByScoreDesc`. Returns target product IDs filtered to the respective `RelationType` and sorted by score descending. Note: there is no `findUpgrades` endpoint — the `UPGRADE` relation type exists in the database model but has no dedicated controller method.

**`buildForPc(productIds: List<Long>): Map<Long, List<Long>>`**

Loads all relations for the given product IDs in a single JPQL `IN` query (`ProductRelationRepository.findBySourceProductIdIn`), groups the results by `sourceProductId` in memory, then returns a map keyed by each input ID. Products with no relations receive an empty list. This avoids N×1 queries that the previous per-product loop implementation required.

**`generateSnapshot(productId: Long, strategy: RecommendationStrategy): RecommendationSnapshot`**

Upsert logic:
1. Resolves the recommended ID list based on strategy:
   - `SIMILAR_ATTRIBUTES` → calls `findByType(productId, RelationType.SIMILAR)`
   - `SAME_CATEGORY` → calls `findAll(productId)` (all relation types)
   - All other strategies (`PRICE_RANGE`, `COMPATIBILITY_BASED`, `POPULAR_IN_CATEGORY`) → fall through to `findAll(productId)` via the `else` branch
2. Looks up an existing snapshot via `findByProductIdAndStrategy`.
3. Saves a new `RecommendationSnapshot`, reusing the existing `id` if one was found (Hibernate update) or generating a new UUID (insert). The `generatedAt` field is always reset to `Instant.now()` on each save because it is set in the constructor and `updatable = false` is not respected during a manual entity replacement.

`generateSnapshot` is not exposed via any REST endpoint — it exists in the service layer only and has no corresponding controller method.

### `RecommendationRuleService`

- `findAll()` — returns all rules via `JpaRepository.findAll()` (no ordering).
- `findById(id)` — throws `ResponseStatusException(404)` if not found.
- `create(dto)` — saves a new `RecommendationRule` entity.
- `update(id, dto)` — loads the existing rule, applies non-null fields from the DTO, saves a replacement entity with the same `id`. Strategy is always preserved from the existing entity.

---

## 6. Inter-Service Communication

No inter-service communication is currently implemented. The service does not declare any HTTP client dependency (`WebClient`, `RestTemplate`, `Feign`, or similar) and makes no outbound calls to other microservices. Product IDs are accepted as plain `Long` values from the caller without validation against the product-service.

---

## 7. Kafka and Asynchronous Events

Kafka is not implemented. No Kafka dependency exists in `build.gradle.kts` and no producers, consumers, or event listeners are present in the codebase.

---

## 8. Error Handling

A `GlobalExceptionHandler` (`@RestControllerAdvice`) handles all exceptions with a consistent JSON response body.

**HTTP status codes in use:**

| Scenario | Status |
|---|---|
| Successful GET | `200 OK` |
| Successful POST (create) | `201 Created` |
| Bean Validation failure (`@Valid`) | `400 Bad Request` |
| Rule not found | `404 Not Found` |
| Unhandled exception | `500 Internal Server Error` |

**Error response format:**
```json
{
  "status": 404,
  "error": "Rule 999 not found"
}
```

**Validation error response format (`400`):**
```json
{
  "status": 400,
  "error": "Validation failed",
  "details": [
    "name: must not be blank",
    "productIds: must not be empty"
  ]
}
```

**Note:** Duplicate `(source_product_id, target_product_id, relation_type)` inserts still produce a `500` — the PostgreSQL unique constraint violation is not caught by the application layer.

---

## 9. Configuration and Deployment

### Environment Variables

| Variable | Default | Required | Description |
|---|---|---|---|
| `RECOMMENDATIONS_SERVER_PORT` | `8085` | No | HTTP server port |
| `RECOMMENDATIONS_API_PREFIX` | `/api` | No | Servlet context path |
| `RECOMMENDATIONS_POSTGRES_HOST` | `localhost` | No | PostgreSQL host |
| `RECOMMENDATIONS_POSTGRES_PORT` | `5432` | No | PostgreSQL port |
| `RECOMMENDATIONS_POSTGRES_DB` | — | **Yes** | Database name |
| `RECOMMENDATIONS_POSTGRES_USER` | — | **Yes** | Database username |
| `RECOMMENDATIONS_POSTGRES_PASSWORD` | — | **Yes** | Database password |
| `RECOMMENDATIONS_HIKARI_POOL_MAX` | `25` | No | HikariCP maximum pool size |
| `RECOMMENDATIONS_HIKARI_POOL_MIN` | `2` | No | HikariCP minimum idle connections |
| `RECOMMENDATIONS_HIKARI_POOL_TIMEOUT_MILLIS` | `30000` | No | HikariCP connection timeout (ms) |
| `RECOMMENDATIONS_POSTGRES_SHOW_SQL` | `true` | No | Log SQL statements |
| `RECOMMENDATIONS_POSTGRES_DDL_AUTO` | `update` | No | Hibernate `hbm2ddl.auto` value |
| `RECOMMENDATIONS_SEED_ENABLE` | `false` | No | Enable data seeding on startup |
| `RECOMMENDATIONS_SEED_MODE` | `init` | No | Seed dataset subdirectory under `static/data/` |
| `RECOMMENDATIONS_SEED_STOP_COUNT` | `1` | No | Skip seeding if row count exceeds this value |

SSL mode is hardcoded to `disable` in `application.yml` and is not configurable via environment variable.

### Data Seeding

Two `CommandLineRunner` beans run on startup when `RECOMMENDATIONS_SEED_ENABLE=true`:

1. `RecommendationRuleDataSeed` (`@Order(1)`) — loads `static/data/{mode}/recommendation_rule.json`
2. `ProductRelationDataSeed` (`@Order(2)`) — loads `static/data/{mode}/product_relation.json`

Both runners skip execution if the target table already has more rows than `RECOMMENDATIONS_SEED_STOP_COUNT`. The bundled `init` dataset contains 5 rules and 6 product relations.

### Docker Build

Multi-stage Dockerfile:

```
Stage 1 (builder): gradle:8.10-jdk21
  WORKDIR /app
  RUN ./gradlew :recommendations-service:bootJar --no-daemon -x test

Stage 2 (runtime): eclipse-temurin:21-jre-alpine
  COPY app.jar from builder
  EXPOSE 8085
  ENTRYPOINT ["java", "-jar", "app.jar"]
```

### docker-compose Entry

```yaml
recommendations-service:
  build:
    context: .
    dockerfile: recommendations-service/Dockerfile
  container_name: recommendations-service
  ports:
    - "8085:8085"
  environment:
    RECOMMENDATIONS_POSTGRES_HOST: postgres-recommendations
    RECOMMENDATIONS_POSTGRES_PORT: 5432
    RECOMMENDATIONS_POSTGRES_DB: recommendations_db
    RECOMMENDATIONS_POSTGRES_USER: postgres
    RECOMMENDATIONS_POSTGRES_PASSWORD: postgres
    RECOMMENDATIONS_POSTGRES_DDL_AUTO: update
    RECOMMENDATIONS_POSTGRES_SHOW_SQL: "false"
    RECOMMENDATIONS_SEED_ENABLE: "false"
  depends_on:
    postgres-recommendations:
      condition: service_healthy
  networks:
    - goshop-network
```

Dependent infrastructure service: `postgres-recommendations` on host port `5436`, internal port `5432`.

### Running Locally

```bash
# Start the database
docker compose up postgres-recommendations -d

# Copy and edit environment variables
cp recommendations-service/.env.sample .env
# Set RECOMMENDATIONS_POSTGRES_DB, RECOMMENDATIONS_POSTGRES_USER, RECOMMENDATIONS_POSTGRES_PASSWORD

# Run the service
./gradlew :recommendations-service:bootRun
```

---

## 10. Testing

No test files exist under `src/test/`. The `spring-boot-starter-test` dependency is declared in `build.gradle.kts` but no test classes have been written. The Dockerfile build skips tests with the `-x test` flag.

---

## 11. Known Limitations and Missing Functionality

### ✅ IMPLEMENTED

- Querying product relations by type and score for the four main recommendation endpoints (`all`, `similar`, `alternatives`, `accessories`).
- **N+1-free `buildForPc`** — single JPQL `IN` query for all product IDs, in-memory grouping; max 100 IDs validated.
- **Bean Validation** (`@NotEmpty`, `@Size(max=100)` on `BuildRecommendationRequest`; `@NotBlank`, `@Size(max=255)` on rule name).
- **Global exception handler** with structured JSON error responses including `details` for validation errors.
- **PostgreSQL indexes** on `product_relations` (source_product_id; source_product_id+relation_type) and `recommendation_snapshots` (product_id; product_id+strategy).
- Full CRUD (create, read-all, update) for `RecommendationRule` with adapter-based DTO mapping.
- Snapshot upsert logic keyed on `(productId, strategy)`.
- HikariCP connection pool with full configuration via environment variables.
- Data seeding via classpath JSON files, guarded by row count and feature flag.
- Stateless security configuration (all endpoints publicly accessible, no CSRF).
- Springdoc OpenAPI UI at `/api/swagger-ui.html`.

### ⚠️ PARTIALLY IMPLEMENTED

- **`generateSnapshot` strategy branching is incomplete.** Only `SIMILAR_ATTRIBUTES` maps to `RelationType.SIMILAR`; `PRICE_RANGE`, `COMPATIBILITY_BASED`, and `POPULAR_IN_CATEGORY` fall through to `findAll()`.
- **`RecommendationRule` is stored but never applied.** `findByEnabledTrueOrderByPriorityDesc()` exists but is called nowhere in the service layer.
- **JSONB `parameters` field is untyped.** The column is `jsonb` in PostgreSQL but the Kotlin field is `String?` — no deserialization or validation occurs.

### ❌ NOT IMPLEMENTED

- **`generateSnapshot` has no REST endpoint.** Snapshots cannot be created or read via the API.
- **No endpoint for `UPGRADE` relation type.** `RelationType.UPGRADE` exists but no `GET /recommendations/products/{id}/upgrades` controller method exists.
- **No `GET /recommendations/rules/{id}` endpoint.** Individual rule lookup is not exposed.
- **No `DELETE` endpoint for rules.**
- **No `ProductRelation` CRUD API.** Relations can only be inserted via the seed mechanism.
- **No test coverage.**
- **Duplicate `(source, target, relation_type)` inserts** result in an unhandled `500` — the unique constraint violation is not caught at the application layer.
- **Kafka / event-driven integration absent.** Snapshots are never invalidated when a product changes.
