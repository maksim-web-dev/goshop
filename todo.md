# GoShop — Sprint B: Microservice Integration and API Aggregation

## 1. Project Context

We are developing GoShop, a B2B computer hardware catalog platform based on Kotlin, Spring Boot, PostgreSQL, and Gradle Multi-Project Build.

The project contains seven implemented microservices:

- `product-service`
- `localization-service`
- `pricing-service`
- `compatibility-service`
- `search-service`
- `recommendations-service`
- `store-service`

The eighth module, `api-service`, is currently a Kotlin stub and must be implemented as a Spring Boot application.

All entity primary keys and cross-service identifiers use `Long`, except natural identifiers such as language codes.

The project is currently running locally with Docker Desktop and Docker Compose. We plan to migrate to Kubernetes later and eventually deploy to AWS.

The long-term goal is to study high-load architecture, horizontal scaling, zero-downtime deployment, observability, and performance optimization.

**Important architectural constraint:** Kafka, Redis, and Flyway are NOT implemented in any service yet.

Do not introduce these technologies in this task. Their implementation will be covered in separate future sprints.

## 2. General Instructions

Before modifying the project:

1. Analyze the current source code of all modules.
2. Read the existing README files and OpenAPI specifications.
3. Read `architecture-review.md`, if available.
4. Examine the actual Controllers, DTOs, Services, Repositories, Entities, SecurityConfig, Dockerfiles, and application configuration.
5. Verify the current state of each item because the documentation may contain outdated information.
6. Preserve working functionality and existing seed JSON data.
7. Maintain the current `Long` identifier strategy.
8. Do not introduce shared databases or cross-service JPA relationships.
9. Do not add direct Gradle dependencies between executable microservices.
10. Do not change existing public API contracts unnecessarily.

Implement the work in the phases below. After each phase, run relevant tests, document the results, and provide a short summary.

Do not silently replace incomplete functionality with mock responses, empty collections, or hardcoded success results.

---

# Phase 1 — product-service

## Goal

Optimize product retrieval for an API aggregation layer and eliminate unnecessary HTTP requests.

### Task 1.1 — Batch product retrieval

Implement:

`POST /api/products/batch`

Request:

```json
{
  "productIds": [1001, 1002, 1003]
}
```

Response:

```json
{
  "products": [
    {
      "id": 1001,
      "sku": "CPU-001",
      "name": "Example CPU",
      "brandId": 1,
      "categoryId": 101,
      "productTypeId": 201,
      "status": "ACTIVE"
    }
  ],
  "notFoundIds": [1003]
}
```

This is a proposed DTO. Adapt it to actual entity fields without inventing stored data.

Requirements:

- One batch request instead of N HTTP requests.
- Prefer one repository query for the requested products.
- Avoid JPA N+1 fetching.
- Limit requests to a configurable maximum, initially 100 product IDs.
- Reject invalid and oversized requests.
- Preserve the requested product order where useful.
- Define explicit behavior for missing products.
- Avoid exposing JPA entities directly.

### Task 1.2 — Detailed product endpoint

Implement or extend:

`GET /api/products/{id}/details`

The response should contain:

- Product information.
- Brand information.
- Category and product type information.
- Product attributes, including stable attribute codes.
- Status and timestamps.

Avoid unnecessary DB queries.

Do not include prices, translations, or recommendations because other microservices own these data.

### Task 1.3 — Additional improvements

- Verify filtering and pagination of `GET /api/products`.
- Add maximum page size.
- Validate request DTOs.
- Verify indexes for SKU, category, product type, and frequently used filters.
- Review potential JPA N+1 problems.
- Review entity relationship fetching strategies.
- Do not change existing entity ID values.

### Acceptance Criteria

- Batch retrieval works for multiple products.
- Empty and oversized batches are handled correctly.
- Missing product IDs are handled predictably.
- Queries do not scale linearly with the number of requested products.
- Unit and integration tests pass.

---

# Phase 2 — localization-service

## Goal

Avoid multiple translation-service requests for every product and every field.

### Task 2.1 — Batch translation resolution

Implement:

`POST /api/translations/batch-resolve`

Example request:

```json
{
  "entityType": "PRODUCT",
  "entityIds": [1001, 1002, 1003],
  "fieldNames": ["name", "description"],
  "languageCode": "uk"
}
```

Example response:

```json
{
  "translations": [
    {
      "entityId": 1001,
      "fieldName": "name",
      "languageCode": "uk",
      "value": "Процесор",
      "found": true
    }
  ],
  "missingTranslations": [
    {
      "entityId": 1003,
      "fieldName": "description",
      "languageCode": "uk"
    }
  ]
}
```

Requirements:

- Batch fetching at the repository level.
- Configurable batch size limit.
- Query only approved translations for public responses.
- Explicit fallback behavior.
- Efficient processing of missing translations.
- No HTTP calls to product-service for every translation.

If the default-language source value is not stored in localization-service, return an explicit missing/fallback result so api-service can use the original product field.

### Task 2.2 — Additional improvements

- Verify uniqueness of entity type + entity ID + field name + language.
- Ensure indexes support batch lookup.
- Validate language codes.
- Verify behavior for disabled languages.
- Add pagination to administrative translation listing.

### Acceptance Criteria

- 100 products × 2 fields can be processed in one HTTP request.
- Translation lookup does not execute one DB query per field.
- Missing translations do not cause errors.
- Tests cover fallback behavior and multiple languages.

---

# Phase 3 — pricing-service

## Goal

Provide efficient batch pricing for a store-specific catalog.

### Task 3.1 — Review existing batch endpoint

Review:

`POST /api/prices/batch`

Verify that it:

- Accepts multiple Long product IDs.
- Accepts storeId.
- Supports currency selection.
- Does not trigger N+1 queries.
- Handles missing prices.
- Properly selects store-specific prices with platform-level fallback.

Do not rewrite a working endpoint unnecessarily.

### Task 3.2 — Batch effective price calculation

Implement:

`POST /api/prices/calculate/batch`

Proposed request:

```json
{
  "productIds": [1001, 1002, 1003],
  "storeId": 2001,
  "currencyCode": "UAH"
}
```

Proposed response:

```json
{
  "prices": [
    {
      "productId": 1001,
      "storeId": 2001,
      "baseAmount": 12000.00,
      "finalAmount": 10800.00,
      "currencyCode": "UAH",
      "discountApplied": true
    }
  ],
  "notFoundIds": []
}
```

Requirements:

- Use BigDecimal.
- Apply explicit rounding rules.
- Respect validFrom / validTo.
- Respect discount start and end dates.
- Validate discount applicability.
- Define behavior when currency conversion is unavailable.
- Ensure no ambiguous overlapping active prices for the same product/store/currency.
- Avoid N+1 database queries.
- Implement appropriate indexes.

### Acceptance Criteria

- Effective prices for 100 products are calculated efficiently.
- Store-specific pricing works.
- Platform-level fallback is correct.
- Expired discounts are ignored.
- Invalid inputs are rejected.
- Tests cover currency conversion and rounding.

---

# Phase 4 — compatibility-service

## Goal

Replace incomplete compatibility-checking logic with functional business rules.

### Task 4.1 — Inter-service product data retrieval

Implement a REST client to product-service for obtaining product characteristics.

Use the existing product details/batch API.

Configure the product-service URL through application configuration and environment variables.

Requirements:

- Connection timeout.
- Response/read timeout.
- Explicit downstream error handling.
- No unlimited retries.
- Bounded concurrency.
- Efficient batch product fetching.

### Task 4.2 — Implement real compatibility rules

Review and implement existing RuleType values:

- SOCKET_MATCH
- RAM_COMPATIBILITY
- POWER_CAPACITY
- GPU_CLEARANCE
- COOLING_TDP
- FORM_FACTOR

Use the actual category parameters and attribute codes defined in product-service.

Do not invent unsupported hardware characteristics.

If a rule cannot be evaluated because required attributes are missing, return an explicit UNKNOWN/INSUFFICIENT_DATA result rather than treating it as compatible.

For aggregate checks, distinguish:

- Compatible
- Incompatible
- Not fully evaluated

### Task 4.3 — Find compatible products

Review:

`GET /api/compatibility/products/{id}`

Determine whether its current implementation genuinely evaluates compatibility or only reads historical successful checks.

Historical compatibility results alone must not be treated as a complete list of compatible products.

Implement a bounded candidate selection and validation strategy, using product-service filtering where possible.

### Acceptance Criteria

- Compatible CPU and motherboard are accepted.
- Incompatible socket combination is rejected.
- Insufficient attributes produce an explicit non-successful evaluation status.
- Error responses identify failed rules.
- Tests cover each implemented rule.
- No request generates unbounded calls to product-service.

---

# Phase 5 — search-service and recommendations-service

## 5A. search-service

### Goal

Make product search suitable for aggregation and large result sets.

Tasks:

1. Review existing PostgreSQL full-text search implementation.
2. Verify ranking and indexing behavior.
3. Verify that attribute JSONB filters are actually applied.
4. Implement or improve pagination.
5. Add maximum page size.
6. Add deterministic sorting.
7. Verify indexes and query execution plans.
8. Ensure archived products are excluded from public results.

### Price filtering

`minPrice` and `maxPrice` currently cannot be applied inside search-service because pricing data belongs to pricing-service.

Do not pretend that filtering is working.

Do not implement naive price filtering by retrieving one page of search results, enriching those products, and filtering afterward. This produces incorrect totals and pagination.

For this sprint:

- Keep price filtering explicitly unsupported if there is no complete implementation.
- Return a clear validation error for unsupported price filters rather than silently ignoring them.
- Document an eventual price-search projection strategy.
- Keep the architecture ready for future synchronization via Kafka.

Do not implement Kafka now.

## 5B. recommendations-service

### Goal

Make recommendation results reliable for api-service aggregation.

Tasks:

1. Review existing recommendation algorithms.
2. Verify that recommended product IDs correspond to real products in seed data.
3. Avoid duplicate recommendations.
4. Add configurable result limits.
5. Add deterministic ranking.
6. Exclude self-recommendations.
7. Review ProductRelation and RecommendationSnapshot.
8. Avoid recalculating expensive results unnecessarily.

Keep the recommendation service responsible for IDs and recommendation scores, not full product/price aggregation.

The api-service will enrich recommendations with catalog and pricing information.

### Acceptance Criteria

- Search provides bounded, correctly ordered results.
- Existing attribute filtering works.
- Price filters are not silently ignored.
- Recommendation IDs correspond to valid products.
- Results are deterministic for identical test data.

---

# Phase 6 — store-service

## Goal

Prepare store authentication for the future public API.

### Task 6.1 — API key validation

Review existing StoreApiKey implementation.

Implement an internal endpoint for validating a store API key if absent.

Suggested contract:

`POST /internal/api-keys/validate`

Do not expose this endpoint publicly without internal-service authentication.

Return minimal validated information:

```json
{
  "valid": true,
  "storeId": 2001,
  "storeStatus": "ACTIVE",
  "defaultLanguage": "uk",
  "defaultCurrency": "UAH",
  "requestsPerSecond": 100,
  "burstLimit": 200
}
```

The returned values must be derived from actual store settings.

Requirements:

- Verify stored key hashes.
- Reject expired, revoked, and inactive keys.
- Reject keys belonging to inactive/suspended stores.
- Do not log raw API keys.
- Do not return API key hashes.
- Prevent timing-sensitive direct string comparisons.
- Ensure store-specific settings are loaded correctly.
- Separate public and internal access.

### Task 6.2 — Security improvements

Review the current `permitAll()` security configuration.

Protect administrative endpoints from unauthorized access.

Add a configurable local development security mechanism, documented explicitly.

Do not use a hardcoded production admin password or unrestricted internal endpoint.

### Acceptance Criteria

- Valid store key is accepted.
- Revoked/expired key is rejected.
- Suspended store is rejected.
- Unauthorized clients cannot modify store settings.
- Security tests pass.

---

# Phase 7 — Common Improvements Across All Seven Services

## 7.1 Validation

Add or complete Bean Validation:

- `@Valid`
- `@NotNull`
- `@NotBlank`
- `@Size`
- `@Min`
- `@Max`
- Appropriate custom validation where necessary.

Validate batch sizes, pagination, and all externally supplied identifiers.

## 7.2 Consistent Error Responses

Review the existing GlobalExceptionHandler.

Extend it carefully to support:

- 400 Bad Request
- 401 Unauthorized
- 403 Forbidden
- 404 Not Found
- 409 Conflict
- 422 Unprocessable Entity where appropriate
- 429 Too Many Requests
- 500 Internal Server Error
- 502 Bad Gateway / 503 Service Unavailable / 504 Gateway Timeout where applicable

Use a consistent error DTO.

Include:

- HTTP status
- Error code
- Message
- Timestamp
- Request/trace ID
- Optional validation details

Do not expose exception stack traces or secrets.

## 7.3 Pagination and Query Limits

For potentially large lists:

- Implement pagination.
- Default page size: 20.
- Configurable maximum page size, initially 100.
- Deterministic sorting.
- Consistent pagination metadata.

Small reference lists may remain unpaginated where justified.

## 7.4 Actuator and Health

Add Spring Boot Actuator to all services.

Expose:

- `/actuator/health`
- `/actuator/health/liveness`
- `/actuator/health/readiness`

Ensure readiness checks required dependencies without making liveness dependent on transient downstream failures.

Do not expose sensitive actuator endpoints publicly.

## 7.5 Graceful Shutdown

Configure graceful shutdown for all Spring Boot applications.

Prepare the applications for rolling deployments and Kubernetes termination.

Check that in-flight requests have an appropriate completion window.

## 7.6 Basic Logging and Metrics

Add:

- Request correlation IDs.
- Structured logging where practical.
- HTTP request duration metrics.
- Downstream HTTP client metrics.
- Error counters.

Do not introduce Redis, Kafka, or a full tracing backend yet.

Use Micrometer-compatible instrumentation so observability can be expanded later.

## 7.7 Database Performance

Review:

- Indexes.
- N+1 queries.
- Unbounded queries.
- Transaction boundaries.
- Connection pool configuration.
- Unnecessary lazy-loading outside transactions.

Do not introduce Flyway in this sprint.

Keep all currently working schema initialization and seed loading mechanisms intact.

Document how schema migrations could be added later without changing them now.

## 7.8 Build and Tests

Ensure each service:

- Builds independently.
- Passes existing tests.
- Has tests for changed API contracts.
- Starts correctly in Docker Compose.
- Has working readiness/liveness endpoints.

Update README and OpenAPI documentation.

---

# Phase 8 — Implement api-service

## Goal

Create a new Spring Boot microservice that provides a single external REST API for stores and aggregates data from the existing services.

The api-service must not directly access databases belonging to other services.

It must not have its own business database for this initial version.

## 8.1 Application Setup

Convert the existing `api-service` Kotlin stub into a Spring Boot application.

Use:

- Kotlin
- Java 21
- Spring Boot version aligned with the other services
- Gradle Kotlin DSL
- Spring Web or WebFlux, choosing one consistent application model
- Spring Validation
- Spring Security
- Spring Boot Actuator
- springdoc-openapi
- Micrometer
- Configurable downstream HTTP clients

Prefer Spring MVC with RestClient and bounded virtual-thread or asynchronous execution where appropriate, unless the existing codebase clearly justifies a reactive stack.

Avoid mixing multiple asynchronous models unnecessarily.

Keep the service stateless.

Default container port: 8080.

## 8.2 Downstream Configuration

All downstream base URLs must be configurable.

Example environment variables:

```text
PRODUCT_SERVICE_URL
LOCALIZATION_SERVICE_URL
PRICING_SERVICE_URL
COMPATIBILITY_SERVICE_URL
SEARCH_SERVICE_URL
RECOMMENDATIONS_SERVICE_URL
STORE_SERVICE_URL
```

For Docker Compose, use internal DNS names such as `http://product-service:8080`, based on the actual service configuration.

Do not hardcode localhost for container-to-container communication.

Configure:

- Connection timeouts.
- Response timeouts.
- Maximum connections.
- Bounded concurrency.
- Connection reuse.
- Explicit fallback behavior.

Do not use unlimited retries.

## 8.3 Store Authentication

Implement a security filter or middleware that:

1. Reads `X-Store-Api-Key`.
2. Validates the key using the protected store-service internal endpoint.
3. Retrieves the authenticated store ID and settings.
4. Places verified store context into the request scope.
5. Uses the verified store ID for downstream operations.

Do not trust a client-provided storeId when it conflicts with the authenticated store.

Do not log raw keys.

Avoid unrestricted public access to downstream service endpoints.

For the first version, document the cost of validating every request against store-service and establish a baseline before adding caching in a later sprint.

## 8.4 API Endpoints

Implement the following aggregated endpoints under `/api/v1`.

### Endpoint A — Product Details

`GET /api/v1/catalog/products/{id}`

Optional query parameters:

- `lang`
- `currency`
- `includeRecommendations`
- `includeAttributes`

Get:

- Product details from product-service.
- Localization from localization-service.
- Effective store price from pricing-service.
- Recommendation IDs from recommendations-service, if requested.

Example response structure:

```json
{
  "product": {
    "id": 1001,
    "sku": "CPU-001",
    "name": "Example CPU",
    "description": "Product description",
    "brand": {
      "id": 1,
      "name": "Example Brand"
    },
    "attributes": []
  },
  "price": {
    "amount": 10800.00,
    "currencyCode": "UAH"
  },
  "recommendations": [],
  "warnings": []
}
```

Use actual product and DTO fields from the source code.

### Endpoint B — Batch Products

`POST /api/v1/catalog/products/batch`

Request:

```json
{
  "productIds": [1001, 1002, 1003],
  "lang": "uk",
  "includePrices": true
}
```

Requirements:

- Use batch endpoints downstream.
- Do not issue N requests for N products.
- Keep output size bounded.
- Support partial missing products.
- Use verified store context.

### Endpoint C — Catalog Search

`GET /api/v1/catalog/search`

Parameters:

- `q`
- `categoryId`
- `brand`
- `lang`
- `page`
- `size`
- `sort`

Flow:

1. Call search-service.
2. Collect product IDs.
3. Fetch missing product metadata in a batch when needed.
4. Fetch translations in a batch.
5. Fetch prices in a batch.
6. Assemble the response.

Do not introduce incorrect price filtering or pagination.

### Endpoint D — Product Recommendations

`GET /api/v1/catalog/products/{id}/recommendations`

Parameters:

- `type`
- `limit`
- `lang`

Flow:

1. Get recommendation IDs.
2. Retrieve products in a batch.
3. Retrieve store-specific prices in a batch.
4. Apply localization.
5. Return enriched recommendation objects.

### Endpoint E — Compatibility Check

`POST /api/v1/catalog/compatibility/check`

Example:

```json
{
  "productIds": [1001, 1005, 1010]
}
```

Flow:

1. Validate request size.
2. Call compatibility-service.
3. Enrich incompatibility issues with product names where useful.
4. Return compatible, incompatible, or insufficient-data status.

Do not treat downstream failures as successful compatibility checks.

### Endpoint F — Catalog Categories

`GET /api/v1/catalog/categories`

Provide localized category information.

Use existing category endpoints.

If a category tree is required but unsupported, implement or document the necessary product-service endpoint explicitly.

## 8.5 Parallel Request Execution

When independent downstream requests can run concurrently, execute them in parallel.

Example for product details:

- Product data — required.
- Price — required when requested.
- Localization — optional with source-language fallback.
- Recommendations — optional.

Requirements:

- Per-call timeout.
- Overall request deadline.
- Cancellation of unnecessary work where supported.
- Bounded concurrent execution.
- No unbounded thread or connection creation.
- Explicit critical vs. optional dependency policies.

Optional recommendation failure must not cause product details to fail.

Price service failure must not silently produce price = 0.

If a requested price is unavailable, distinguish missing price from a service outage.

## 8.6 API Rate Limiting

Implement an initial local/in-memory rate-limiting solution if it can be introduced cleanly.

Use authenticated store settings:

- requestsPerSecond
- burstLimit

Return HTTP 429 when the limit is exceeded.

Important:

An in-memory limiter is only accurate for a single instance. Clearly document that it does not enforce a global per-store limit across multiple Kubernetes replicas.

Do not introduce Redis now.

Design the implementation behind a RateLimiter interface so it can later be replaced by a distributed solution.

## 8.7 Security

Separate:

- Public store catalog API.
- Internal service-to-service endpoints.
- Administrative APIs.

Do not create a generic unauthenticated reverse proxy to every internal service.

Ensure rate-limiting and authentication decisions use verified store identity.

Validate supported language and currency overrides.

## 8.8 API Documentation

Generate OpenAPI documentation for api-service.

For each endpoint provide:

- Query/path parameters.
- Request DTO.
- Response DTO.
- Error responses.
- Validation constraints.
- Examples.
- Authentication requirements.

## 8.9 Docker Integration

Update Docker Compose.

Ensure:

- api-service has its own Dockerfile.
- api-service starts on port 8080.
- product-service remains accessible using its actual internal port.
- All downstream URLs are configured correctly.
- Existing services and databases remain functional.
- No dependency on Redis or Kafka is introduced.

## 8.10 Testing

Implement:

### Unit Tests

- Aggregation logic.
- Fallback logic.
- Authentication mapping.
- Rate-limiting logic.
- Language selection.
- Price availability handling.
- Error response mapping.

### Integration Tests

Use mocked HTTP downstream servers for:

- Product detail aggregation.
- Batch search enrichment.
- Recommendation enrichment.
- Compatibility requests.
- Invalid API keys.
- Downstream timeouts.
- Partial failures.
- Malformed downstream responses.

### Docker Compose Smoke Tests

Verify:

1. All eight services start.
2. All services expose health endpoints.
3. api-service can reach each downstream service.
4. Store authentication works.
5. A valid product can be retrieved with translation and price.
6. Product batch lookup works.
7. Catalog search works.
8. Recommendations return enriched results.
9. Compatibility checks return real evaluation results.
10. Unavailable optional services are handled correctly.

---

# Phase 9 — Final Verification and Documentation

After implementation:

1. Run a clean Gradle build.
2. Run all unit tests.
3. Run available integration tests.
4. Validate OpenAPI contracts.
5. Verify Docker Compose startup.
6. Check that seed data remains compatible.
7. Verify that no UUID mismatch has been reintroduced.
8. Verify that Kafka, Redis, and Flyway were not accidentally added.
9. Check for N+1 HTTP and JPA queries.
10. Review API security and internal endpoint exposure.

Update service READMEs.

Create:

`docs/sprint-b-report.md`

Include:

- Completed tasks.
- Changed files.
- New endpoints.
- Changed DTOs.
- New database indexes or entity changes.
- Tests executed and results.
- Verified end-to-end scenarios.
- Remaining limitations.
- Known performance risks.
- Recommendations for the next sprint.

Use these statuses:

- IMPLEMENTED
- PARTIALLY IMPLEMENTED
- NOT IMPLEMENTED
- BLOCKED

Do not mark a task IMPLEMENTED solely because code was written; it must pass relevant tests.

---

# Execution Rules

Execute phases sequentially.

After every phase:

- Explain what was changed.
- List affected modules.
- Run tests.
- Update documentation.
- Report unresolved issues.

Stop and request a decision if a phase requires a breaking API change, destructive database migration, or a major architectural choice not covered by this specification.

Do not start Kafka, Redis, Flyway, OpenSearch migration, Kubernetes deployment, or AWS integration as part of this task.

The final result should be a functioning GoShop API aggregation layer based on the seven existing services, with consistent contracts, testable behavior, and a foundation for future performance optimization.