# GoShop Architecture Review

**Date:** 2026-11-06
**Reviewer:** Architect Agent (Claude Sonnet 4.6)
**Scope:** Full source-code analysis of all 7 backend services + api-service stub in the monorepo at `/home/max/IdeaProjects/goshop`.

---

## 1. Services Overview Table

| Service | Port | DB name | Responsibilities | PK type | Spring Boot version |
|---|---|---|---|---|---|
| **product-service** | 8080 (host: 8081 in docker-compose) | `product_db` | Product catalog: brands, categories, product types, components, parameter groups, product attributes | `Long` (IDENTITY) | 3.3.5 |
| **compatibility-service** | 8082 | `compatibility_db` | PC component compatibility rules, compatibility checks, issue tracking | `UUID` (GenerationType.UUID) | 3.3.5 |
| **localization-service** | 8083 | `localization_db` | Translations of entity fields (product, brand, category, product_type) across BCP 47 languages; translation jobs | `UUID` (GenerationType.UUID) | 3.3.5 |
| **pricing-service** | 8084 | `pricing_db` | Prices (platform-level and per-store), discounts (PERCENTAGE/FIXED), price history, exchange rates, price calculation | `UUID` (GenerationType.UUID) | 3.3.5 |
| **recommendations-service** | 8085 | `recommendations_db` | Product relations (similar/alternative/accessory/upgrade), recommendation rules, pre-computed snapshots | `UUID` (GenerationType.UUID) | 3.3.5 |
| **search-service** | 8086 | `search_db` | Full-text search over PostgreSQL FTS (`search_documents` table), autocomplete suggestions, re-index job queue | `UUID` (GenerationType.UUID for jobs; `product_id UUID` PK for documents) | 3.3.5 |
| **store-service** | 8087 | `store_db` | Store management, per-store settings (rate limits, allowed languages), API key lifecycle (BCrypt hash, prefix), webhooks | `UUID` (GenerationType.UUID) | 3.3.5 |
| **api-service** | 8080 | — | Stub only — single `Main.kt` with a "Hello, Kotlin!" print loop; no Spring context | — | — (plain Kotlin module) |

**Notes:**
- All 7 implemented services use Kotlin 2.0.0 (product-service uses 2.2.0 for the JVM plugin), Spring Boot 3.3.5, JDK 21.
- product-service is the **only** service using `Long` (AUTO INCREMENT) as its primary key type.
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

Cross-service references exist only as bare UUID fields stored in the database (e.g., `productId: UUID` in pricing, compatibility, search, recommendations). These are not enforced at the code level.

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
| `CategoryParameter` | `category_parameters` | `Long` | References `Category`, `ParameterGroup` |
| `ParameterGroup` | `parameter_groups` | `Long` | — |
| `ParameterGroupItem` | `parameter_group_items` | `Long` | References `ParameterGroup` |
| `ProductAttribute` | `product_attributes` | `Long` | References `Product`, `CategoryParameter`; value stored as `TEXT` |
| `Component` | `components` | `Long` | References `ComponentGroup` |
| `ComponentGroup` | `component_groups` | `Long` | — |

#### compatibility-service (all PKs are `UUID`, `GenerationType.UUID`)

| Entity | Table | PK | Cross-service UUID refs |
|---|---|---|---|
| `CompatibilityRule` | `compatibility_rules` | `UUID` | `sourceProductTypeId: UUID`, `targetProductTypeId: UUID` → product-service `ProductType` |
| `CompatibilityCheck` | `compatibility_checks` | `UUID` | `productIds: List<UUID>` (collection table) → product-service `Product` |
| `CompatibilityIssue` | `compatibility_issues` | `UUID` | Refs `CompatibilityCheck`, `CompatibilityRule` (local) |

#### localization-service (all PKs are `UUID` or String BCP-47 code)

| Entity | Table | PK | Cross-service UUID refs |
|---|---|---|---|
| `Language` | `languages` | `String` (BCP-47 code, e.g. `uk`) | — |
| `Translation` | `translations` | `UUID` | `entityId: UUID` — references PRODUCT, BRAND, CATEGORY, PRODUCT_TYPE in other services |
| `TranslationJob` | `translation_jobs` | `UUID` | `entityId: UUID` — same cross-service reference |

#### pricing-service (all PKs are `UUID`, `GenerationType.UUID`)

| Entity | Table | PK | Cross-service UUID refs |
|---|---|---|---|
| `Price` | `prices` | `UUID` | `productId: UUID` → product-service `Product`; `storeId: UUID?` → store-service `Store` |
| `Discount` | `discounts` | `UUID` | `storeId: UUID` → store-service `Store`; `productId: UUID?` → product-service `Product` |
| `PriceHistory` | `price_history` | `UUID` | References `Price` (local) |
| `ExchangeRate` | `exchange_rates` | `UUID` | — |

#### recommendations-service (all PKs are `UUID`, `GenerationType.UUID`)

| Entity | Table | PK | Cross-service UUID refs |
|---|---|---|---|
| `ProductRelation` | `product_relations` | `UUID` | `sourceProductId: UUID`, `targetProductId: UUID` → product-service `Product` |
| `RecommendationRule` | `recommendation_rules` | `UUID` | — |
| `RecommendationSnapshot` | `recommendation_snapshots` | `UUID` | `productId: UUID`, `recommendedProductIds: List<UUID>` → product-service `Product` |

#### search-service

| Entity | Table | PK | Cross-service UUID refs |
|---|---|---|---|
| `SearchDocument` | `search_documents` | `UUID` (`product_id`) | `productId: UUID` → product-service `Product`; `categoryId: UUID` → product-service `Category` |
| `SearchIndexJob` | `search_index_jobs` | `UUID` | `productId: UUID` → product-service `Product` |

#### store-service (all PKs are `UUID`, `GenerationType.UUID`)

| Entity | Table | PK | Cross-service UUID refs |
|---|---|---|---|
| `Store` | `stores` | `UUID` | — |
| `StoreSettings` | `store_settings` | `UUID` | References `Store` (local `@OneToOne`) |
| `StoreApiKey` | `store_api_keys` | `UUID` | References `Store` (local `@ManyToOne`) |
| `StoreWebhook` | `store_webhooks` | `UUID` | References `Store` (local) |

### 3.2 Cross-Service UUID Reference Summary

| Reference field | Lives in service | Points to service | PK type at target | Mismatch? |
|---|---|---|---|---|
| `productId: UUID` | compatibility, pricing, recommendations, search | product-service `Product.id` | **Long** | **YES — critical type mismatch** |
| `sourceProductTypeId: UUID` | compatibility | product-service `ProductType.id` | **Long** | **YES — critical type mismatch** |
| `targetProductTypeId: UUID` | compatibility | product-service `ProductType.id` | **Long** | **YES — critical type mismatch** |
| `categoryId: UUID` | search | product-service `Category.id` | **Long** | **YES — critical type mismatch** |
| `entityId: UUID` | localization | product-service (`Product`, `Brand`, `Category`, `ProductType`) | **Long** | **YES — critical type mismatch** |
| `storeId: UUID` | pricing | store-service `Store.id` | UUID | No mismatch |

### 3.3 Summary of Type Mismatch

**product-service** uses auto-increment `Long` PKs for all entities. Every other service references products, brands, categories and product types using `UUID`. This is the most critical architectural inconsistency in the entire codebase: an api-service or any integration layer cannot join or correlate data across services without a lookup or mapping mechanism.

---

## 4. API Contract Inconsistencies

### 4.1 Product ID Type Mismatch (Critical)

- **product-service** exposes `id: Long` in `ProductResponse`, `BrandResponse`, `CategoryResponse`, `ProductTypeResponse`.
- **All other services** accept and store these cross-references as `UUID`.
- When a client asks for price by `productId` (UUID), there is no product with that UUID in product-service.
- **Impact:** api-service cannot call pricing/compatibility/recommendations/search with the product ID returned by product-service without a resolution layer.

### 4.2 Docker-Compose Port Discrepancy

- product-service application runs on port 8080 (from `application.yml`), but docker-compose publishes it to **host port 8081**.
- The assignment specification table states product-service should be on 8080, but the api-service container occupies that host port.
- This is inconsistent across the specification vs. deployment configuration.

### 4.3 Error Response Format — No Standard Body

None of the services defines a global `@ControllerAdvice` / `@ExceptionHandler`. All error responses are produced by Spring Boot's default `BasicErrorController` or by `ResponseStatusException` (which produces whitelabel error responses). There is no uniform error body format (e.g., `{ "code": "NOT_FOUND", "message": "..." }`).

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

### 4.7 Incomplete `ProductAttributeController`

`PUT /products/{productId}/attributes` returns `Unit` (no response body), while all other mutation endpoints return the updated entity. This violates consistency.

### 4.8 `PostgresConfig` Bug — Wrong Package Scan

In every service, `PostgresConfig.kt` has:
```kotlin
setPackagesToScan("zero44.crm.core.data.postgres")
```
This is **copy-pasted from an unrelated project** and scans a package that does not exist. JPA entities are still found because Spring Boot's auto-configuration also scans `com.data.entity` via `@ComponentScan`. However this is fragile and the explicit entity manager factory likely has no entity classes registered from its own scan — it falls back to the boot auto-config context. This should be `setPackagesToScan("com.data.entity")`.

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
| `GET /api/recommendations/products/{id}` with enrichment | recommendations + product + pricing | Recommended product UUIDs enriched with product data and prices |

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
| `GET /recommendations/products/{id}/accessories` with pricing | Currently returns List<UUID>; api-service needs enriched responses |
| `POST /recommendations/products/build` exists for PC build — no way to associate built products with a store's pricing | Needs `storeId` parameter |

### 7.5 search-service

| Missing Endpoint | Reason Needed |
|---|---|
| `GET /search/products?storeId=` (store-scoped search) | Currently no store filtering in search |
| `localizedNames` field not populated in search query results | `SearchService.search()` returns empty `localizedNames` map; FTS is on English/simple text only |
| Filter by min/max price | `SearchQueryDto` has `minPrice`/`maxPrice` fields but `SearchService.search()` does not apply them in SQL |

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
3. GET pricing-service /prices/products/{uuid}    → ~1 DB query  (UUID/Long mismatch blocks this)
4. POST localization-service /translations/resolve × N fields → N DB queries
5. GET recommendations-service /recommendations/products/{uuid}/similar → ~1 DB query
6. GET recommendations-service /recommendations/products/{uuid}/accessories → ~1 DB query
```

Minimum **6 serial HTTP calls** on the critical path with current API design. With parallel execution (steps 2–6 can be fanned out after step 1), it reduces to **2 sequential hops**, but requires the UUID/Long mismatch to be resolved first.

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

## 10. Recommendations (NOT YET IMPLEMENTED)

> All items below represent **future work** — none are currently implemented. Items are ordered by priority.

### 10.1 Critical Gaps Blocking api-service Integration

**P0 — Must fix before any inter-service integration is possible:**

1. **Resolve the Long vs UUID PK mismatch in product-service.**
   - Option A (Recommended): Add a `uuid` column to `products`, `brands`, `categories`, `product_types` as a secondary stable external identifier. Expose it in all API responses. Downstream services reference this UUID. The internal Long PK is kept for JPA/DB performance.
   - Option B: Migrate product-service to UUID PKs entirely (larger change, requires data migration).
   - Without this, compatibility, localization, pricing, recommendations and search cannot reliably reference product-service entities.

2. **Fix the `PostgresConfig.setPackagesToScan("zero44.crm.core.data.postgres")` copy-paste bug** in all services. Change to `"com.data.entity"`. This may cause subtle issues with the custom `EntityManagerFactory` missing entity classes.

3. **Implement the api-service** as a proper Spring Boot application with:
   - Spring Cloud Gateway or Spring MVC-based routing.
   - `WebClient` / `RestClient` beans for each downstream service.
   - Store API key authentication middleware.

4. **Add `POST /translations/batch-resolve`** to localization-service. Without this, rendering a list of products requires N×fields HTTP calls.

5. **Add `GET /products/by-ids`** (batch) to product-service. Without it, recommendations and search enrichment cause N HTTP calls.

6. **Fix stub implementations:**
   - `CompatibilityCheckService.evaluateRule()` always returns `null` — all compatibility checks trivially pass.
   - `CompatibilityCheckService.findCompatibleProducts()` always returns `emptyList()`.
   - `SearchService.search()` ignores `minPrice` / `maxPrice` filter parameters.
   - `SearchDocument.localizedNames` is not populated on query results.

### 10.2 Cross-Cutting Concerns

7. **Authentication & Authorization.** All services use `SecurityConfig` with `.anyRequest().permitAll()` — there is no authentication at all. For production:
   - Internal service-to-service calls should use a shared secret or mTLS.
   - Public endpoints require store API key validation (store-service has the key management logic).
   - Admin endpoints require a separate admin role.

8. **Distributed Tracing.** Add `spring-boot-starter-actuator` + Micrometer Tracing + OpenTelemetry to all services. Without trace IDs, debugging cross-service failures is very difficult.

9. **Structured Logging.** Add a standard MDC with `traceId`, `storeId`, `serviceVersion` to all log entries. Currently logging is default Spring Boot with no correlation context.

10. **Global Error Handler.** Add a `@ControllerAdvice` with a uniform error response format (`{ "status": 404, "code": "NOT_FOUND", "message": "..." }`) to all services.

11. **Input Validation.** No `@Valid`, `@NotNull`, `@Size` or Bean Validation annotations are used in any request DTO. Invalid requests (e.g., null `productId` in `PriceCreateRequest`) will cause unhandled NullPointerException or Hibernate constraint violations with 500 responses.

### 10.3 Data Consistency Issues

12. **No distributed transaction support.** When an api-service creates a product (product-service) and immediately creates its price (pricing-service), there is no saga/compensation mechanism. A partial failure leaves the system in an inconsistent state.

13. **search_documents is manually managed.** Currently the only way to update search data is via `POST /internal/search/reindex` with explicit product IDs. There is no trigger from product-service when a product is created/updated. Data in search will be stale unless a Kafka event pipeline or scheduled synchronisation is added.

14. **Translation consistency.** `Translation.entityId` stores a UUID, but product-service uses Long IDs. Translations created today cannot be reliably resolved to actual products until the UUID mismatch is fixed.

15. **Pricing and product lifecycle.** When a product is archived in product-service, its prices in pricing-service remain active. There is no mechanism to deactivate prices when a product is archived.

### 10.4 Performance Improvements

16. **Add `spring-boot-starter-actuator`** to all services to expose `/actuator/health`, `/actuator/metrics`, and `/actuator/prometheus`. This is prerequisite for Kubernetes health probes and Prometheus scraping.

17. **Add Spring Cache (`@Cacheable`) or Redis** for:
    - Exchange rates (change infrequently).
    - Language list.
    - Store settings (rate limits used on every request).
    - Product category tree.

18. **Pagination on unbounded list endpoints.** `GET /translations`, `GET /discounts`, `GET /brands`, etc. should be paginated or limited. At scale, `translationRepository.findAll()` could return millions of rows.

19. **Dockerfile layer caching.** Change Dockerfiles to copy `gradle*`, `build.gradle.kts`, `settings.gradle.kts` first and run a dependency download step before copying source code. This will make incremental builds significantly faster.

20. **Connection pool sizing.** The default `PRODUCT_HIKARI_POOL_MAX=25` is defined for every service. With 7 services each holding up to 25 connections, that is 175 connections to 7 separate Postgres instances. This is reasonable but should be tuned per service based on actual load.

### 10.5 Long-Term Architectural Evolution

21. **Introduce Kafka for event-driven integration.** Key events:
    - `product.created` / `product.updated` / `product.archived` (produced by product-service) → consumed by search-service (re-index), pricing-service (validate productId), recommendations-service (clean stale relations).
    - `price.updated` (produced by pricing-service) → consumed by search-service to update search documents.
    - `store.deactivated` (produced by store-service) → consumed by pricing-service to deactivate store prices.
    This would eliminate the need for the api-service to orchestrate data consistency.

22. **Align product-service PK strategy** with the rest of the platform. The long-term target should be UUID PKs throughout, as this simplifies distributed ID generation, enables global deduplication, and removes the ambiguity that currently affects 5 out of 6 downstream services.

23. **Consider a dedicated API gateway** (Kong, Nginx, or Spring Cloud Gateway) in front of the api-service for rate limiting (store-service already stores `requestsPerSecond` and `burstLimit` per store), TLS termination, and request logging.

24. **Kubernetes manifests.** Create Helm charts or Kustomize overlays for each service with:
    - `readinessProbe` on `/actuator/health/readiness` (requires actuator).
    - `livenessProbe` on `/actuator/health/liveness`.
    - Resource requests/limits.
    - `PodDisruptionBudget` for HA deployments.

25. **Database migrations.** Currently `ddl-auto=update` is used in all services. This is not safe for production — schema drift is irreversible and there is no migration history. Replace with Flyway or Liquibase with versioned migration scripts.
