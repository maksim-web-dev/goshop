# GoShop Architecture Review

**Date:** 2026-11-06 (updated 2026-11-06)
**Reviewer:** Architect Agent (Claude Sonnet 4.6)
**Scope:** Full source-code analysis of all 7 backend services + api-service stub in the monorepo at `/home/max/IdeaProjects/goshop`.

---

## Changelog

| Date | Changes |
|---|---|
| 2026-11-06 | Initial review |
| 2026-11-06 | UUID→Long migration completed in all 6 non-product services; `GlobalExceptionHandler` added to all 7 services; `PostgresConfig.setPackagesToScan` bug fixed in all 7 services; `ProductAttributeController` returns body; seed data infrastructure rewritten with JdbcTemplate + explicit IDs; `search_documents` schema updated (BIGINT, JSONB columns) |

---

## 1. Services Overview Table

| Service | Port | DB name | Responsibilities | PK type | Spring Boot version |
|---|---|---|---|---|---|
| **product-service** | 8080 (host: 8081 in docker-compose) | `product_db` | Product catalog: brands, categories, product types, components, parameter groups, product attributes | `Long` (IDENTITY) | 3.3.5 |
| **compatibility-service** | 8082 | `compatibility_db` | PC component compatibility rules, compatibility checks, issue tracking | `Long` (IDENTITY) | 3.3.5 |
| **localization-service** | 8083 | `localization_db` | Translations of entity fields (product, brand, category, product_type) across BCP 47 languages; translation jobs | `Long` (IDENTITY) | 3.3.5 |
| **pricing-service** | 8084 | `pricing_db` | Prices (platform-level and per-store), discounts (PERCENTAGE/FIXED), price history, exchange rates, price calculation | `Long` (IDENTITY) | 3.3.5 |
| **recommendations-service** | 8085 | `recommendations_db` | Product relations (similar/alternative/accessory/upgrade), recommendation rules, pre-computed snapshots | `Long` (IDENTITY) | 3.3.5 |
| **search-service** | 8086 | `search_db` | Full-text search over PostgreSQL FTS (`search_documents` table), autocomplete suggestions, re-index job queue | `Long` (IDENTITY; `product_id BIGINT` PK for documents) | 3.3.5 |
| **store-service** | 8087 | `store_db` | Store management, per-store settings (rate limits, allowed languages), API key lifecycle (BCrypt hash, prefix), webhooks | `Long` (IDENTITY) | 3.3.5 |
| **api-service** | 8080 | — | Stub only — single `Main.kt` with a "Hello, Kotlin!" print loop; no Spring context | — | — (plain Kotlin module) |

**Notes:**
- All 7 implemented services use Kotlin 2.0.0 (product-service uses 2.2.0 for the JVM plugin), Spring Boot 3.3.5, JDK 21.
- All services now use `Long` (AUTO INCREMENT / IDENTITY) as their primary key type. The UUID→Long migration has been completed across all 6 non-product services.
- The docker-compose maps product-service container port 8080 to **host port 8081**, not 8080 as specified in the assignment table. Port 8080 on the host is reserved for the api-service container.

---

## 2. Inter-Service Dependency Matrix

> Based exclusively on source code. No HTTP clients (`RestTemplate`, `WebClient`, `FeignClient`, `RestClient`) and no Kafka producers/consumers were found in any service.

| Caller \ Called | product | compatibility | localization | pricing | recommendations | search | store |
|---|---|---|---|---|---|---|---|
| **product** | — | — | — | — | — | — | — |
| **compatibility** | — | — | — | — | — | — | — |
| **localization** | — | — | — | — | — | — | — |
| **pricing** | — | — | — | — | — | — | — |
| **recommendations** | — | — | — | — | — | — | — |
| **search** | — | — | — | — | — | — | — |
| **store** | — | — | — | — | — | — | — |

**All services are fully isolated — no synchronous or asynchronous inter-service calls are implemented.**

Cross-service references exist only as bare `Long` fields stored in the database (e.g., `productId: Long` in pricing, compatibility, search, recommendations). These are not enforced at the code level.

---

## 3. Entity Identifier Inventory

### 3.1 Per-Service Entity Inventory

#### product-service (all PKs are `Long`, `GenerationType.IDENTITY`)

| Entity | Table | PK | Notes |
|---|---|---|---|
| `Product` | `products` | `Long` | Has `createdAt`, `updatedAt` (`Instant`) |
| `Brand` | `brands` | `Long` | No audit timestamps |
| `Category` | `categories` | `Long` | Self-referencing parent; no timestamps |
| `ProductType` | `product_types` | `Long` | References `Category` |
| `CategoryParameter` | `category_parameters` | `Long` | References `Category`, `ParameterGroup`; has `code: String?` field |
| `ParameterGroup` | `parameter_groups` | `Long` | — |
| `ParameterGroupItem` | `parameter_group_items` | `Long` | References `ParameterGroup` |
| `ProductAttribute` | `product_attributes` | `Long` | References `Product`, `CategoryParameter`; value stored as `TEXT` |
| `Component` | `components` | `Long` | References `ComponentGroup` |
| `ComponentGroup` | `component_groups` | `Long` | — |

#### compatibility-service (all PKs are `Long`, `GenerationType.IDENTITY`)

| Entity | Table | PK | Cross-service Long refs |
|---|---|---|---|
| `CompatibilityRule` | `compatibility_rules` | `Long` | `sourceProductTypeId: Long`, `targetProductTypeId: Long` → product-service `ProductType` |
| `CompatibilityCheck` | `compatibility_checks` | `Long` | `productIds: List<Long>` (collection table) → product-service `Product` |
| `CompatibilityIssue` | `compatibility_issues` | `Long` | Refs `CompatibilityCheck`, `CompatibilityRule` (local) |

#### localization-service (PKs are `Long` or String BCP-47 code)

| Entity | Table | PK | Cross-service Long refs |
|---|---|---|---|
| `Language` | `languages` | `String` (BCP-47 code, e.g. `uk`) | — |
| `Translation` | `translations` | `Long` | `entityId: Long` — references PRODUCT, BRAND, CATEGORY, PRODUCT_TYPE in other services |
| `TranslationJob` | `translation_jobs` | `Long` | `entityId: Long` — same cross-service reference |

#### pricing-service (all PKs are `Long`, `GenerationType.IDENTITY`)

| Entity | Table | PK | Cross-service Long refs |
|---|---|---|---|
| `Price` | `prices` | `Long` | `productId: Long` → product-service `Product`; `storeId: Long?` → store-service `Store` |
| `Discount` | `discounts` | `Long` | `storeId: Long` → store-service `Store`; `productId: Long?` → product-service `Product` |
| `PriceHistory` | `price_history` | `Long` | References `Price` (local) |
| `ExchangeRate` | `exchange_rates` | `Long` | — |

#### recommendations-service (all PKs are `Long`, `GenerationType.IDENTITY`)

| Entity | Table | PK | Cross-service Long refs |
|---|---|---|---|
| `ProductRelation` | `product_relations` | `Long` | `sourceProductId: Long`, `targetProductId: Long` → product-service `Product` |
| `RecommendationRule` | `recommendation_rules` | `Long` | — |
| `RecommendationSnapshot` | `recommendation_snapshots` | `Long` | `productId: Long`, `recommendedProductIds: List<Long>` → product-service `Product` |

#### search-service

| Entity | Table | PK | Cross-service Long refs |
|---|---|---|---|
| `SearchDocument` | `search_documents` | `Long` (`product_id BIGINT`) | `productId: Long` → product-service `Product`; `categoryId: Long` → product-service `Category` |
| `SearchIndexJob` | `search_index_jobs` | `Long` | `productId: Long` → product-service `Product` |

Schema updated: `search_documents` table now has `localized_names JSONB` and `attributes JSONB` columns in addition to the core FTS fields.

#### store-service (all PKs are `Long`, `GenerationType.IDENTITY`)

| Entity | Table | PK | Cross-service refs |
|---|---|---|---|
| `Store` | `stores` | `Long` | — |
| `StoreSettings` | `store_settings` | `Long` | References `Store` (local `@OneToOne`) |
| `StoreApiKey` | `store_api_keys` | `Long` | References `Store` (local `@ManyToOne`) |
| `StoreWebhook` | `store_webhooks` | `Long` | References `Store` (local) |

### 3.2 Cross-Service Reference Summary

> **RESOLVED:** All services now use `Long` PKs. All cross-service references are consistent.

| Reference field | Lives in service | Points to service | PK type at target | Mismatch? |
|---|---|---|---|---|
| `productId: Long` | compatibility, pricing, recommendations, search | product-service `Product.id` | `Long` | No |
| `sourceProductTypeId: Long` | compatibility | product-service `ProductType.id` | `Long` | No |
| `targetProductTypeId: Long` | compatibility | product-service `ProductType.id` | `Long` | No |
| `categoryId: Long` | search | product-service `Category.id` | `Long` | No |
| `entityId: Long` | localization | product-service (`Product`, `Brand`, `Category`, `ProductType`) | `Long` | No |
| `storeId: Long` | pricing | store-service `Store.id` | `Long` | No |

### 3.3 PK Type Consistency — Resolved

**All services now use `Long` (AUTO INCREMENT) PKs for all entities.** The UUID→Long migration was completed by changing `GenerationType.UUID` to `GenerationType.IDENTITY` and all cross-service reference fields from `UUID` to `Long` in every non-product service.

Seed data infrastructure was correspondingly updated: all `DataSeed` classes in product-service were rewritten to use `JdbcTemplate` with explicit SQL `INSERT ... ON CONFLICT (id) DO NOTHING` + `setval(pg_get_serial_sequence(...))` to support stable cross-service Long IDs (products 1001–1015, stores 2001–2003, product_types 201–208, categories 100–108).

---

## 4. API Contract Inconsistencies

### 4.1 Product ID Type Mismatch — RESOLVED

~~product-service exposes `id: Long` while all other services store these references as `UUID`.~~

**Fixed.** All 6 non-product services were migrated from UUID to Long. `productId`, `storeId`, `sourceProductTypeId`, `targetProductTypeId`, `entityId`, and `categoryId` fields in all services are now `Long`, matching the product-service and store-service PKs. The api-service can now pass product IDs across services without any type conversion.

### 4.2 Docker-Compose Port Discrepancy

- product-service application runs on port 8080 (from `application.yml`), but docker-compose publishes it to **host port 8081**.
- The assignment specification table states product-service should be on 8080, but the api-service container occupies that host port.
- This is inconsistent across the specification vs. deployment configuration.

### 4.3 Error Response Format — RESOLVED

~~None of the services defines a global `@ControllerAdvice` / `@ExceptionHandler`.~~

**Fixed.** `GlobalExceptionHandler.kt` (annotated with `@RestControllerAdvice`) was added to the `com.rest` package in all 7 services. It provides a uniform error body format:
```json
{ "status": 404, "error": "Not found" }
```
Handles both `ResponseStatusException` and generic `Exception`.

### 4.4 Missing `createdAt` / `updatedAt` on Several Entities

| Entity | Service | Missing Fields |
|---|---|---|
| `Brand` | product | `createdAt`, `updatedAt` |
| `Category` | product | `createdAt`, `updatedAt` |
| `ProductType` | product | `createdAt`, `updatedAt` |
| `CategoryParameter` | product | `createdAt`, `updatedAt` |
| `ParameterGroup` | product | `createdAt`, `updatedAt` |
| `ParameterGroupItem` | product | `createdAt`, `updatedAt` |
| `Component` / `ComponentGroup` | product | `createdAt`, `updatedAt` |
| `ProductAttribute` | product | `createdAt`, `updatedAt` |
| `CompatibilityRule` | compatibility | `createdAt`, `updatedAt` |
| `ProductRelation` | recommendations | `createdAt`, `updatedAt` |
| `RecommendationRule` | recommendations | `createdAt`, `updatedAt` |
| `Discount` | pricing | `createdAt`, `updatedAt` |
| `ExchangeRate` | pricing | `updatedAt` (has `effectiveAt`) |
| `StoreWebhook` | store | `createdAt`, `updatedAt` |

### 4.5 Naming Inconsistencies

| Issue | Location |
|---|---|
| Update endpoint uses `PATCH` in some services (`product`, `brand`, `category`, `store`, `compatibility-rule`), `PUT` in others (`pricing Price`, `discount`, `recommendation-rule`, `translation`) — not standardised | Multiple services |
| `CompatibilityRuleController.disable()` calls `DELETE /{id}` but semantically soft-disables (sets `enabled = false`), not hard-deletes — misleading HTTP verb | compatibility-service |
| `RecommendationRuleController` has no `DELETE` endpoint at all, while `CompatibilityRuleController` soft-deletes via `DELETE` | recommendations-service |
| `ProductController.archive()` is mapped to `DELETE /{id}` but is a soft-archive — same misleading verb pattern | product-service |
| `StoreController.deactivate()` is mapped to `DELETE /{id}` — same pattern | store-service |
| `SearchController` has `GET /search/products` with query params AND `POST /search/products/query` for structured queries — duplicated with different HTTP methods | search-service |

### 4.6 Missing Pagination in List Endpoints

Most list endpoints return `List<T>` with no pagination:
- `GET /brands` — unbounded list
- `GET /categories` — unbounded list
- `GET /product-types` — unbounded list
- `GET /components` — unbounded list
- `GET /parameter-groups` — unbounded list
- `GET /discounts` — unbounded list
- `GET /translations` — unbounded list (potentially very large)
- `GET /recommendations/rules` — unbounded list

Only `GET /products` is paginated.

### 4.7 Incomplete `ProductAttributeController` — RESOLVED

~~`PUT /products/{productId}/attributes` returns `Unit` (no response body).~~

**Fixed.** `setProductAttributes()` in `ProductAttributeService` now returns `List<ProductAttribute>`, and `ProductAttributeController.setAttributes()` returns `ResponseEntity<List<ProductAttributeResponse>>`, consistent with other mutation endpoints.

### 4.8 `PostgresConfig` Bug — Wrong Package Scan — RESOLVED

~~In every service, `PostgresConfig.kt` has `setPackagesToScan("zero44.crm.core.data.postgres")`.~~

**Fixed.** All 7 services now have `setPackagesToScan("com.data.entity")` in their `PostgresConfig.kt`.

---

## 5. Kafka Events

**No Kafka implementation found in any service.**

No Kafka dependency is declared in any `build.gradle.kts`. No `@KafkaListener`, `@KafkaHandler`, `KafkaTemplate`, or producer/consumer configurations exist anywhere in the codebase. All services are strictly synchronous and self-contained.

---

## 6. Required api-service Capabilities

The api-service module currently contains only an IDE-generated `Main.kt` stub with no Spring context. Based on what clients would need from an aggregation layer, the following capabilities are required:

### 6.1 Routing Table

| Path prefix | Target service | Port |
|---|---|---|
| `/api/products/**` | product-service | 8080 |
| `/api/brands/**` | product-service | 8080 |
| `/api/categories/**` | product-service | 8080 |
| `/api/product-types/**` | product-service | 8080 |
| `/api/category-parameters/**` | product-service | 8080 |
| `/api/parameter-groups/**` | product-service | 8080 |
| `/api/components/**` | product-service | 8080 |
| `/api/prices/**` | pricing-service | 8084 |
| `/api/discounts/**` | pricing-service | 8084 |
| `/api/exchange-rates/**` | pricing-service | 8084 |
| `/api/compatibility/**` | compatibility-service | 8082 |
| `/api/translations/**` | localization-service | 8083 |
| `/api/translation-jobs/**` | localization-service | 8083 |
| `/api/languages/**` | localization-service | 8083 |
| `/api/recommendations/**` | recommendations-service | 8085 |
| `/api/search/**` | search-service | 8086 |
| `/api/stores/**` | store-service | 8087 |

### 6.2 Aggregated Endpoints Required

| Aggregated Endpoint | Data Sources | Description |
|---|---|---|
| `GET /api/products/{id}/full` | product + pricing + localization + recommendations | Single call returning product detail with current prices, translations for user's locale, and similar/accessory product IDs |
| `GET /api/search/products?q=...&storeId=...&lang=...` | search + pricing (batch) + localization | Search results enriched with prices and translated names |
| `POST /api/compatibility/check` with product enrichment | compatibility + product | Check result enriched with product names |
| `GET /api/recommendations/products/{id}` with enrichment | recommendations + product + pricing | Recommended product IDs enriched with product data and prices |

### 6.3 Store Authentication Middleware

The `store-service` already implements API key generation (`StoreApiKeyService` with BCrypt hashing). The api-service must:
1. Extract the `X-Store-Api-Key` header on each request.
2. Call `store-service` to validate the key hash and retrieve the `storeId`.
3. Inject `storeId` into downstream requests (e.g., for pricing, settings).

This is not yet implemented anywhere.

### 6.4 Language / Currency Resolution

The api-service must resolve:
- `Accept-Language` header → language code for localization-service calls.
- Store's `defaultCurrency` from store-service → currency code for pricing-service calls.

---

## 7. Missing Endpoints / Batch Operations

Based on the actual APIs implemented, the following endpoints are absent but will be required for api-service integration:

### 7.1 product-service

| Missing Endpoint | Reason Needed |
|---|---|
| `GET /products/by-ids` (batch by list of Long IDs) | api-service needs to hydrate multiple product IDs returned by recommendations/search at once |
| `GET /products/{id}/full` (product + attributes in one call) | Avoids N+1: currently `GET /products/{id}` + `GET /products/{id}/attributes` are separate |
| `GET /brands/{id}` (exists) but no `GET /brands/by-ids` batch | Same batch hydration need |
| `GET /categories/{id}` (exists) but no `GET /categories/tree` | Category tree needed for storefront navigation |

### 7.2 pricing-service

| Missing Endpoint | Reason Needed |
|---|---|
| `POST /prices/batch` (exists as `POST /prices/batch`) | Already implemented — good |
| `POST /prices/calculate/batch` (batch calculate for multiple products) | Search results page needs effective price for each product simultaneously |
| `GET /discounts?storeId=&productId=` (filtered discounts) | Currently `GET /discounts` returns all with no filtering |

### 7.3 localization-service

| Missing Endpoint | Reason Needed |
|---|---|
| `POST /translations/batch-resolve` (resolve multiple fields at once) | Product detail page needs name + description translations in one call; currently requires N calls to `POST /translations/resolve` |
| `GET /translations?entityIds[]=...` (batch by multiple entity IDs) | Search results need translations for multiple products |

### 7.4 recommendations-service

| Missing Endpoint | Reason Needed |
|---|---|
| `GET /recommendations/products/{id}/accessories` with pricing | Currently returns `List<Long>`; api-service needs enriched responses |
| `POST /recommendations/products/build` exists for PC build — no way to associate built products with a store's pricing | Needs `storeId` parameter |

### 7.5 search-service

| Missing Endpoint | Reason Needed |
|---|---|
| `GET /search/products?storeId=` (store-scoped search) | Currently no store filtering in search |
| ~~`localizedNames` not populated in search results~~ | **RESOLVED.** `SearchService.search()` now SELECTs `localized_names` and `attributes` JSONB columns and maps them to response. `SearchResultResponse` now includes both fields. |
| ~~`indexProduct()` does not write JSONB columns~~ | **RESOLVED.** `indexProduct()` now writes `localizedNames` and `attributes` via `?::jsonb` parameters. |
| Attribute filter not applied in search | `SearchQueryDto.attributes` map is now applied as `attributes @> ?::jsonb` containment filter in SQL. **RESOLVED.** |
| Filter by min/max price | `SearchQueryDto` has `minPrice`/`maxPrice` fields but they cannot be applied — `search_documents` has no price column. Price data lives in pricing-service. Requires either schema denormalization or post-query filtering via pricing-service batch call. |

### 7.6 compatibility-service

| Missing Endpoint | Reason Needed |
|---|---|
| `GET /compatibility/products/{id}` already returns compatible product IDs, but `findCompatibleProducts()` always returns `emptyList()` | Implementation is a stub |
| `evaluateRule()` in `CompatibilityCheckService` is a stub — always returns `null` (pass) | All compatibility checks trivially succeed regardless of products |

---

## 8. Synchronous Communication Bottlenecks

### 8.1 Critical-Path Calls for Product Detail Page

A typical "product detail" page in an api-service request would require:

```
1. GET product-service /products/{id}            → ~1 DB query
2. GET product-service /products/{id}/attributes  → ~1 DB query  (separate HTTP call)
3. GET pricing-service /prices/products/{id}      → ~1 DB query  (Long IDs now consistent)
4. POST localization-service /translations/resolve × N fields → N DB queries
5. GET recommendations-service /recommendations/products/{id}/similar → ~1 DB query
6. GET recommendations-service /recommendations/products/{id}/accessories → ~1 DB query
```

Minimum **6 serial HTTP calls** on the critical path with current API design. With parallel execution (steps 2–6 can be fanned out after step 1), it reduces to **2 sequential hops**.

### 8.2 N+1 Patterns

| Scenario | N+1 Risk |
|---|---|
| Search results page (20 products) | 20 calls to `GET /prices/products/{id}` unless `POST /prices/batch` is used (batch endpoint exists) |
| Search results with translations | 20 × 2 (name + description) = 40 calls to `POST /translations/resolve` — no batch endpoint exists |
| Recommendation list (10 products) | 10 calls to product-service to hydrate names — no batch endpoint exists |
| Compatibility check with rule evaluation | Currently stub; full implementation would require product attribute lookups |

### 8.3 Recommendations

1. **Add `POST /translations/batch-resolve`** in localization-service — this is the highest-priority bottleneck.
2. **Add `GET /products/by-ids`** in product-service — needed for recommendation and search hydration.
3. **`POST /prices/batch`** already exists in pricing-service — use it for all multi-product pages.
4. For product detail, make api-service issue steps 2–6 in parallel using `CompletableFuture` or Kotlin coroutines.
5. Consider adding a Redis or Caffeine cache in api-service for product metadata (brands, categories) which change infrequently.

---

## 9. Docker and Kubernetes Readiness

### 9.1 Dockerfile Presence

| Service | Dockerfile | Notes |
|---|---|---|
| product-service | ✅ | Multi-stage: `gradle:8.10-jdk21` builder + `eclipse-temurin:21-jre-alpine` runtime |
| compatibility-service | ✅ | Same pattern |
| localization-service | ✅ | Same pattern |
| pricing-service | ✅ | Same pattern |
| recommendations-service | ✅ | Same pattern |
| search-service | ✅ | Same pattern |
| store-service | ✅ | Same pattern |
| api-service | ✅ (entry in docker-compose) | Dockerfile exists but api-service has no Spring Boot plugin; build will fail |

**Note:** All Dockerfiles copy the **entire monorepo** into the builder (`COPY . .`) and run only the specific service's `bootJar` task. This means every image build re-downloads all Gradle dependencies and compiles all modules. Build caching is ineffective.

### 9.2 docker-compose Entry with Correct Env Vars

| Service | docker-compose entry | Env vars correct | Health check for DB |
|---|---|---|---|
| product-service | ✅ | ✅ | ✅ (`depends_on: service_healthy`) |
| compatibility-service | ✅ | ✅ | ✅ |
| localization-service | ✅ | ✅ | ✅ |
| pricing-service | ✅ | ✅ | ✅ |
| recommendations-service | ✅ | ✅ | ✅ |
| search-service | ✅ | ✅ | ✅ |
| store-service | ✅ | ✅ | ✅ |
| api-service | ✅ (entry present) | ❌ (no env vars; stub only) | ❌ (no depends_on) |

### 9.3 Application-Level Health Check

| Service | Spring Actuator dependency declared | Health endpoint available |
|---|---|---|
| All 7 services | ❌ | ❌ |

None of the `build.gradle.kts` files include `spring-boot-starter-actuator`. There is no `/actuator/health` endpoint. The docker-compose service entries have no `healthcheck` stanzas for the application containers themselves (only for Postgres).

### 9.4 Graceful Shutdown

No explicit graceful shutdown configuration (`server.shutdown=graceful`, `spring.lifecycle.timeout-per-shutdown-phase`) is set in any `application.yml`. Spring Boot 3.x defaults to immediate shutdown unless configured.

### 9.5 Kubernetes Readiness / Liveness Probes

| Service | Readiness probe | Liveness probe |
|---|---|---|
| All services | ❌ | ❌ |

No Kubernetes manifests (Deployment, Service, Ingress) exist in the repository. Without actuator, there is no standard health endpoint to probe against.

---

## 10. Recommendations (Status)

> Items are ordered by priority. Resolved items are marked.

### 10.1 Critical Gaps Blocking api-service Integration

**P0 — Must fix before any inter-service integration is possible:**

1. ~~**Resolve the Long vs UUID PK mismatch in product-service.**~~ **RESOLVED.** All 6 non-product services migrated from UUID→Long. Option B (full migration) was chosen. All `productId`, `storeId`, `entityId`, `categoryId`, `sourceProductTypeId`, `targetProductTypeId` fields are now `Long` throughout the codebase.

2. ~~**Fix the `PostgresConfig.setPackagesToScan("zero44.crm.core.data.postgres")` copy-paste bug.**~~ **RESOLVED.** All 7 services now use `setPackagesToScan("com.data.entity")`.

3. **Implement the api-service** as a proper Spring Boot application with:
   - Spring Cloud Gateway or Spring MVC-based routing.
   - `WebClient` / `RestClient` beans for each downstream service.
   - Store API key authentication middleware.

4. **Add `POST /translations/batch-resolve`** to localization-service. Without this, rendering a list of products requires N×fields HTTP calls.

5. **Add `GET /products/by-ids`** (batch) to product-service. Without it, recommendations and search enrichment cause N HTTP calls.

6. **Fix stub implementations:**
   - `CompatibilityCheckService.evaluateRule()` always returns `null` — all compatibility checks trivially pass. **Cannot be implemented without HTTP client to product-service** (needs product attributes to evaluate socket match, RAM compatibility, etc.). Requires inter-service integration.
   - ~~`CompatibilityCheckService.findCompatibleProducts()` always returns `emptyList()`.~~ **RESOLVED.** Now queries `compatibility_checks` history: returns distinct product IDs that appeared together with the given product in a compatible check.
   - `SearchService.search()` ignores `minPrice` / `maxPrice` filter parameters. **Cannot be implemented** — `search_documents` has no price column. Price data lives in pricing-service only.
   - ~~`SearchService.search()` does not SELECT `localized_names`/`attributes` JSONB columns.~~ **RESOLVED.** `SearchService.search()` now SELECTs both JSONB columns and maps them to `SearchDocument.localizedNames` / `SearchDocument.attributes`. The `attributes` map from `SearchQueryDto` is applied as a PostgreSQL JSONB containment filter (`attributes @> ?::jsonb`).
   - ~~`SearchService.indexProduct()` does not write `localized_names`/`attributes` to the new JSONB columns.~~ **RESOLVED.** `indexProduct()` now writes both JSONB columns; `SearchResultResponse` and `SearchResultResponseAdapter` updated to expose `localizedNames` and `attributes` in API responses.

### 10.2 Cross-Cutting Concerns

7. **Authentication & Authorization.** All services use `SecurityConfig` with `.anyRequest().permitAll()` — there is no authentication at all. For production:
   - Internal service-to-service calls should use a shared secret or mTLS.
   - Public endpoints require store API key validation (store-service has the key management logic).
   - Admin endpoints require a separate admin role.

8. **Distributed Tracing.** Add `spring-boot-starter-actuator` + Micrometer Tracing + OpenTelemetry to all services. Without trace IDs, debugging cross-service failures is very difficult.

9. **Structured Logging.** Add a standard MDC with `traceId`, `storeId`, `serviceVersion` to all log entries. Currently logging is default Spring Boot with no correlation context.

10. ~~**Global Error Handler.**~~ **RESOLVED.** `GlobalExceptionHandler` (`@RestControllerAdvice`) added to all 7 services. Returns `{ "status": <code>, "error": "<message>" }` for both `ResponseStatusException` and unhandled `Exception`.

11. **Input Validation.** No `@Valid`, `@NotNull`, `@Size` or Bean Validation annotations are used in any request DTO. Invalid requests (e.g., null `productId` in `PriceCreateRequest`) will cause unhandled NullPointerException or Hibernate constraint violations with 500 responses.

### 10.3 Data Consistency Issues

12. **No distributed transaction support.** When an api-service creates a product (product-service) and immediately creates its price (pricing-service), there is no saga/compensation mechanism. A partial failure leaves the system in an inconsistent state.

13. **search_documents is manually managed.** Currently the only way to update search data is via `POST /internal/search/reindex` with explicit product IDs. There is no trigger from product-service when a product is created/updated. Data in search will be stale unless a Kafka event pipeline or scheduled synchronisation is added. The reindex endpoint now correctly populates `localized_names` and `attributes` JSONB columns.

14. **Pricing and product lifecycle.** When a product is archived in product-service, its prices in pricing-service remain active. There is no mechanism to deactivate prices when a product is archived.

### 10.4 Performance Improvements

15. **Add `spring-boot-starter-actuator`** to all services to expose `/actuator/health`, `/actuator/metrics`, and `/actuator/prometheus`. This is prerequisite for Kubernetes health probes and Prometheus scraping.

16. **Add Spring Cache (`@Cacheable`) or Redis** for:
    - Exchange rates (change infrequently).
    - Language list.
    - Store settings (rate limits used on every request).
    - Product category tree.

17. **Pagination on unbounded list endpoints.** `GET /translations`, `GET /discounts`, `GET /brands`, etc. should be paginated or limited. At scale, `translationRepository.findAll()` could return millions of rows.

18. **Dockerfile layer caching.** Change Dockerfiles to copy `gradle*`, `build.gradle.kts`, `settings.gradle.kts` first and run a dependency download step before copying source code. This will make incremental builds significantly faster.

19. **Connection pool sizing.** The default `PRODUCT_HIKARI_POOL_MAX=25` is defined for every service. With 7 services each holding up to 25 connections, that is 175 connections to 7 separate Postgres instances. This is reasonable but should be tuned per service based on actual load.

### 10.5 Long-Term Architectural Evolution

20. **Introduce Kafka for event-driven integration.** Key events:
    - `product.created` / `product.updated` / `product.archived` (produced by product-service) → consumed by search-service (re-index), pricing-service (validate productId), recommendations-service (clean stale relations).
    - `price.updated` (produced by pricing-service) → consumed by search-service to update search documents.
    - `store.deactivated` (produced by store-service) → consumed by pricing-service to deactivate store prices.
    This would eliminate the need for the api-service to orchestrate data consistency.

21. **Consider a dedicated API gateway** (Kong, Nginx, or Spring Cloud Gateway) in front of the api-service for rate limiting (store-service already stores `requestsPerSecond` and `burstLimit` per store), TLS termination, and request logging.

22. **Kubernetes manifests.** Create Helm charts or Kustomize overlays for each service with:
    - `readinessProbe` on `/actuator/health/readiness` (requires actuator).
    - `livenessProbe` on `/actuator/health/liveness`.
    - Resource requests/limits.
    - `PodDisruptionBudget` for HA deployments.

23. **Database migrations.** Currently `ddl-auto=update` is used in all services. This is not safe for production — schema drift is irreversible and there is no migration history. Replace with Flyway or Liquibase with versioned migration scripts.
