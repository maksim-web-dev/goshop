# Localization Service

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

The `localization-service` is responsible for storing and serving translations of localizable text fields that belong to entities managed by other microservices in the GoShop platform.

**Core responsibilities:**

- Persist translated values for named fields (e.g. `name`, `description`) of remote entities (products, brands, categories, product types).
- Serve translations filtered by entity type, entity ID, and language code.
- Resolve the best available translation for a given entity field, with an explicit preferred-language and fallback-language strategy.
- Track asynchronous translation jobs that represent requests to translate an entity into a target language. The jobs are created and stored; execution is not implemented.

**Data ownership:**

The service owns the `languages`, `translations`, and `translation_jobs` tables. It does not own entity records themselves — it references them by cross-service Long ID (`entityId`) without foreign keys to other services' databases.

---

## 2. Technology Stack

| Component | Version / Artifact |
|---|---|
| Kotlin | 2.0.0 |
| JVM toolchain | 21 (eclipse-temurin:21-jre-alpine in Docker) |
| Spring Boot | 3.3.5 |
| Spring Dependency Management plugin | 1.1.6 |
| `spring-boot-starter-web` | (managed by Spring Boot 3.3.5) |
| `spring-boot-starter-data-jpa` | (managed by Spring Boot 3.3.5) |
| `spring-boot-starter-security` | (managed by Spring Boot 3.3.5) |
| `jackson-module-kotlin` | (managed by Spring Boot 3.3.5) |
| `springdoc-openapi-starter-webmvc-ui` | 2.6.0 |
| PostgreSQL JDBC driver | (managed by Spring Boot 3.3.5, runtime only) |
| HikariCP | (transitive via spring-boot-starter-data-jpa) |
| Hibernate / Jakarta Persistence | (transitive via spring-boot-starter-data-jpa) |
| `spring-boot-starter-test` | (test scope) |

Build system: Gradle with Kotlin DSL (`build.gradle.kts`).

---

## 3. Database Model

### 3.1 Enum Types

**`EntityType`** — the type of the remote entity a translation belongs to:

| Value |
|---|
| `PRODUCT` |
| `BRAND` |
| `CATEGORY` |
| `PRODUCT_TYPE` |

**`TranslationStatus`** — the lifecycle state of a translated value:

| Value | Meaning |
|---|---|
| `DRAFT` | Created but not reviewed |
| `APPROVED` | Manually reviewed and confirmed |
| `AUTO_TRANSLATED` | Produced by an automated translation provider |

**`TranslationJobStatus`** — the execution state of a translation job:

| Value | Meaning |
|---|---|
| `PENDING` | Job created, not yet picked up |
| `IN_PROGRESS` | Currently being executed |
| `DONE` | Successfully completed |
| `FAILED` | Execution failed |

---

### 3.2 Entity: `Language`

Table: `languages`

| Column | Kotlin type | Constraints | Notes |
|---|---|---|---|
| `code` | `String` | `@Id`, NOT NULL | BCP 47 language code (e.g. `en`, `uk`, `de`). No auto-generation — the caller supplies the value. |
| `name` | `String` | NOT NULL | Human-readable language name (e.g. `English`, `Українська`). |
| `enabled` | `Boolean` | NOT NULL, default `true` | Whether the language is active and returned by the list endpoint. |

---

### 3.3 Entity: `Translation`

Table: `translations`

| Column | Kotlin type | Constraints | Notes |
|---|---|---|---|
| `id` | `Long` | `@Id`, auto `GenerationType.IDENTITY` | Primary key. |
| `entity_type` | `EntityType` | NOT NULL, stored as `STRING` | References the type of the owning entity. |
| `entity_id` | `Long` | NOT NULL | ID of the owning entity in its origin service (cross-service reference, no FK). |
| `field_name` | `String` | NOT NULL | Name of the localizable field, e.g. `name` or `description`. |
| `language_code` | `Language` (FK) | NOT NULL, `@ManyToOne LAZY` | Join column `language_code` referencing `languages.code`. |
| `translated_value` | `String` | NOT NULL, `columnDefinition = "TEXT"` | The translated text content. |
| `status` | `TranslationStatus` | NOT NULL, stored as `STRING`, default `DRAFT` | Lifecycle state of this translation. |
| `updated_at` | `Instant` | NOT NULL | Timestamp of last modification. |

**Unique constraint:** `(entity_type, entity_id, field_name, language_code)` — one translation record per field per language per entity.

---

### 3.4 Entity: `TranslationJob`

Table: `translation_jobs`

| Column | Kotlin type | Constraints | Notes |
|---|---|---|---|
| `id` | `Long` | `@Id`, auto `GenerationType.IDENTITY` | Primary key. |
| `entity_type` | `EntityType` | NOT NULL, stored as `STRING` | Type of the entity to be translated. |
| `entity_id` | `Long` | NOT NULL | ID of the entity to be translated. |
| `target_language_code` | `Language` (FK) | NOT NULL, `@ManyToOne LAZY` | Join column `target_language_code` referencing `languages.code`. |
| `status` | `TranslationJobStatus` | NOT NULL, stored as `STRING`, default `PENDING` | Current execution status. |
| `attempts` | `Int` | NOT NULL, default `0` | Number of execution attempts made. |
| `created_at` | `Instant` | NOT NULL, `updatable = false` | Creation timestamp. |
| `completed_at` | `Instant?` | nullable | Set when the job reaches `DONE` or `FAILED` status. |

---

## 4. REST API Endpoints

All endpoints are served under the context path `/api` (configurable via `LOCALIZATION_API_PREFIX`).

Security: all requests are permitted without authentication (Spring Security is configured with `permitAll()` and stateless sessions).

---

### 4.1 `GET /api/languages`

**Controller:** `LanguageController.getAll()`

**Purpose:** Returns all languages with `enabled = true`.

**Query parameters:** none.

**Request body:** none.

**Response body:** `List<LanguageResponse>`

| Field | Type |
|---|---|
| `code` | `String` |
| `name` | `String` |
| `enabled` | `Boolean` |

**HTTP status codes:**

| Code | Condition |
|---|---|
| `200 OK` | Always (empty array if no enabled languages exist) |

**Example response:**

```json
[
  { "code": "en", "name": "English",    "enabled": true },
  { "code": "uk", "name": "Українська", "enabled": true },
  { "code": "de", "name": "Deutsch",    "enabled": true },
  { "code": "pl", "name": "Polski",     "enabled": true }
]
```

---

### 4.2 `GET /api/translations`

**Controller:** `TranslationController.getAll()`

**Purpose:** Returns translations. When both `entityType` and `entityId` are provided, the result is filtered to that entity. Otherwise all translation records are returned.

**Query parameters:**

| Parameter | Type | Required | Notes |
|---|---|---|---|
| `entityType` | `EntityType` | No | One of `PRODUCT`, `BRAND`, `CATEGORY`, `PRODUCT_TYPE` |
| `entityId` | `Long` | No | ID of the entity |

Note: filtering only activates when **both** parameters are present. If only one is supplied, all translations are returned.

**Request body:** none.

**Response body:** `List<TranslationResponse>`

| Field | Type |
|---|---|
| `id` | `Long` |
| `entityType` | `EntityType` |
| `entityId` | `Long` |
| `fieldName` | `String` |
| `languageCode` | `String` |
| `translatedValue` | `String` |
| `status` | `TranslationStatus` |
| `updatedAt` | `Instant` (ISO-8601) |

**HTTP status codes:**

| Code | Condition |
|---|---|
| `200 OK` | Always |

**Example request:**

```
GET /api/translations?entityType=PRODUCT&entityId=1001
```

**Example response:**

```json
[
  {
    "id": 1,
    "entityType": "PRODUCT",
    "entityId": 1001,
    "fieldName": "name",
    "languageCode": "en",
    "translatedValue": "GPU NVIDIA GeForce RTX 4090",
    "status": "APPROVED",
    "updatedAt": "2024-10-01T12:00:00Z"
  }
]
```

---

### 4.3 `POST /api/translations`

**Controller:** `TranslationController.create()`

**Purpose:** Creates a new translation record.

**Request body:** `TranslationCreateRequest`

| Field | Type | Required | Notes |
|---|---|---|---|
| `entityType` | `EntityType` | Yes | One of the four enum values |
| `entityId` | `Long` | Yes | ID of the entity in its origin service |
| `fieldName` | `String` | Yes | Name of the localizable field |
| `languageCode` | `String` | Yes | BCP 47 code; must exist in the `languages` table |
| `translatedValue` | `String` | Yes | Translated text content |

Initial `status` is always set to `DRAFT` by the service layer.

**Response body:** `TranslationResponse` (same fields as listed in 4.2).

**HTTP status codes:**

| Code | Condition |
|---|---|
| `201 Created` | Translation created successfully |
| `404 Not Found` | `languageCode` does not exist |
| `500 Internal Server Error` | Duplicate unique key `(entity_type, entity_id, field_name, language_code)` (no explicit 409 handler exists) |

**Example request:**

```json
{
  "entityType": "PRODUCT",
  "entityId": 1001,
  "fieldName": "name",
  "languageCode": "de",
  "translatedValue": "Grafikkarte NVIDIA GeForce RTX 4090"
}
```

**Example response:**

```json
{
  "id": 99,
  "entityType": "PRODUCT",
  "entityId": 1001,
  "fieldName": "name",
  "languageCode": "de",
  "translatedValue": "Grafikkarte NVIDIA GeForce RTX 4090",
  "status": "DRAFT",
  "updatedAt": "2024-10-01T12:05:00Z"
}
```

**Possible errors:**

- `404` with message `Language <code> not found` — when the supplied `languageCode` has no record in the database.

---

### 4.4 `PUT /api/translations/{id}`

**Controller:** `TranslationController.update()`

**Purpose:** Updates `translatedValue` and/or `status` of an existing translation. Fields not supplied in the body retain their current values (partial update pattern). `updatedAt` is always refreshed to `Instant.now()`.

**Path variables:**

| Variable | Type | Required |
|---|---|---|
| `id` | `Long` | Yes |

**Request body:** `TranslationUpdateRequest`

| Field | Type | Required | Notes |
|---|---|---|---|
| `translatedValue` | `String?` | No | Omit to keep the current value |
| `status` | `TranslationStatus?` | No | Omit to keep the current status |

**Response body:** `TranslationResponse` (see 4.2).

**HTTP status codes:**

| Code | Condition |
|---|---|
| `200 OK` | Updated successfully |
| `404 Not Found` | No translation with the given `id` |

**Example request:**

```json
{ "status": "APPROVED" }
```

**Example response:**

```json
{
  "id": 99,
  "entityType": "PRODUCT",
  "entityId": 1001,
  "fieldName": "name",
  "languageCode": "de",
  "translatedValue": "Grafikkarte NVIDIA GeForce RTX 4090",
  "status": "APPROVED",
  "updatedAt": "2024-10-01T13:00:00Z"
}
```

**Possible errors:**

- `404` with message `Translation <id> not found`.

---

### 4.5 `DELETE /api/translations/{id}`

**Controller:** `TranslationController.delete()`

**Purpose:** Permanently deletes a translation record.

**Path variables:**

| Variable | Type | Required |
|---|---|---|
| `id` | `Long` | Yes |

**Request body:** none.

**Response body:** none.

**HTTP status codes:**

| Code | Condition |
|---|---|
| `204 No Content` | Deleted successfully |
| `404 Not Found` | No translation with the given `id` |

**Possible errors:**

- `404` with message `Translation <id> not found`.

---

### 4.6 `POST /api/translations/resolve`

**Controller:** `TranslationController.resolve()`

**Purpose:** Resolves the best available translation for a specific entity field, using a preferred language with an explicit fallback. Returns the first match found — preferred language first, then fallback language. Throws `404` if neither is available.

**Request body:** `ResolveTranslationRequest`

| Field | Type | Required | Default | Notes |
|---|---|---|---|---|
| `entityType` | `EntityType` | Yes | — | One of the four enum values |
| `entityId` | `Long` | Yes | — | ID of the entity |
| `fieldName` | `String` | Yes | — | Name of the field to resolve |
| `preferredLanguage` | `String` | Yes | — | BCP 47 code of the preferred language |
| `fallbackLanguage` | `String` | No | `"en"` | BCP 47 code used when preferred is unavailable |

**Response body:** `TranslationResponse` (see 4.2) — a single object, not a list.

**HTTP status codes:**

| Code | Condition |
|---|---|
| `200 OK` | A translation was found in preferred or fallback language |
| `404 Not Found` | No translation exists in either language |

**Example request:**

```json
{
  "entityType": "PRODUCT",
  "entityId": 1001,
  "fieldName": "name",
  "preferredLanguage": "de",
  "fallbackLanguage": "en"
}
```

**Example response:**

```json
{
  "id": 1,
  "entityType": "PRODUCT",
  "entityId": 1001,
  "fieldName": "name",
  "languageCode": "en",
  "translatedValue": "GPU NVIDIA GeForce RTX 4090",
  "status": "APPROVED",
  "updatedAt": "2024-10-01T12:00:00Z"
}
```

(In the above example, German was not found, so the English fallback was returned.)

**Possible errors:**

- `404` with message `No translation found for <entityType>/<entityId>/<fieldName>`.

---

### 4.7 `POST /api/translation-jobs`

**Controller:** `TranslationJobController.create()`

**Purpose:** Creates a new translation job record with status `PENDING`. The job represents a request to translate an entity into the given target language. No actual translation execution is triggered.

**Request body:** `TranslationJobCreateRequest`

| Field | Type | Required | Notes |
|---|---|---|---|
| `entityType` | `EntityType` | Yes | One of the four enum values |
| `entityId` | `Long` | Yes | ID of the entity to translate |
| `targetLanguageCode` | `String` | Yes | BCP 47 code; must exist in the `languages` table |

**Response body:** `TranslationJobResponse`

| Field | Type | Notes |
|---|---|---|
| `id` | `Long` | Generated job ID |
| `entityType` | `EntityType` | |
| `entityId` | `Long` | |
| `targetLanguageCode` | `String` | Resolved from the `Language` entity |
| `status` | `TranslationJobStatus` | Always `PENDING` on creation |
| `attempts` | `Int` | Always `0` on creation |
| `createdAt` | `Instant` | ISO-8601 |
| `completedAt` | `Instant?` | Always `null` on creation |

**HTTP status codes:**

| Code | Condition |
|---|---|
| `201 Created` | Job created successfully |
| `404 Not Found` | `targetLanguageCode` does not exist in the `languages` table |

**Example request:**

```json
{
  "entityType": "PRODUCT",
  "entityId": 1001,
  "targetLanguageCode": "de"
}
```

**Example response:**

```json
{
  "id": 1,
  "entityType": "PRODUCT",
  "entityId": 1001,
  "targetLanguageCode": "de",
  "status": "PENDING",
  "attempts": 0,
  "createdAt": "2024-10-01T14:00:00Z",
  "completedAt": null
}
```

**Possible errors:**

- `404` with message `Language <code> not found`.

---

### 4.8 `GET /api/translation-jobs/{id}`

**Controller:** `TranslationJobController.getById()`

**Purpose:** Returns the current state of a translation job by its ID.

**Path variables:**

| Variable | Type | Required |
|---|---|---|
| `id` | `Long` | Yes |

**Request body:** none.

**Response body:** `TranslationJobResponse` (same fields as listed in 4.7).

**HTTP status codes:**

| Code | Condition |
|---|---|
| `200 OK` | Job found |
| `404 Not Found` | No job with the given `id` |

**Possible errors:**

- `404` with message `Job <id> not found`.

---

## 5. Business Logic

### 5.1 TranslationService

**`findAll(entityType, entityId)`**
When both `entityType` and `entityId` are non-null, delegates to `TranslationRepository.findByEntityTypeAndEntityId()`. When either parameter is null, returns all translations via `findAll()`.

**`findById(id)`**
Looks up by UUID; throws `ResponseStatusException(404)` if absent.

**`create(dto)`**
Loads the `Language` entity by `dto.languageCode` (404 if missing), then saves a new `Translation` with `status = DRAFT` and `updatedAt = Instant.now()`.

**`update(id, dto)`**
Loads the existing translation, then saves a new `Translation` instance with the same `id`, `entityType`, `entityId`, `fieldName`, and `language`, but with `translatedValue` and `status` replaced by the DTO values when present, and `updatedAt` refreshed.

**`delete(id)`**
Checks existence first (404 if missing), then deletes by ID.

**`resolve(dto)` — fallback logic**

```
1. Query translations table for (entityType, entityId, fieldName, preferredLanguage)
2. If found → return it
3. Else query for (entityType, entityId, fieldName, fallbackLanguage)
4. If found → return it
5. Else throw ResponseStatusException(404)
```

The default value of `fallbackLanguage` in `ResolveTranslationRequest` is `"en"`. The two lookups are executed as separate database queries; there is no JPA join or single-query resolution.

### 5.2 TranslationJobService

**`create(dto)`**
Loads the target `Language` by `dto.targetLanguageCode` (404 if missing), then saves a `TranslationJob` with `status = PENDING` and `attempts = 0`. No background processing, scheduler, or worker is invoked.

**`findById(id)`**
Looks up by UUID; throws `ResponseStatusException(404)` if absent.

**`TranslationJobRepository.findByStatus(status)`**
Defined in the repository but not called from any service or controller. It is available for future use by a worker or scheduler.

### 5.3 LanguageService

**`findAll()`** — returns only languages with `enabled = true` via `LanguageRepository.findByEnabledTrue()`.

**`findByCode(code)`** — returns a single language by BCP 47 code (404 if absent). This method is defined but not called from any controller or service in the current codebase.

---

## 6. Inter-Service Communication

No inter-service communication is currently implemented. The service does not make any HTTP calls to other microservices. Entity IDs in the `translations` and `translation_jobs` tables are cross-service references stored as plain `Long` values with no runtime validation against the owning service.

---

## 7. Kafka and Asynchronous Events

Kafka is not implemented. The `build.gradle.kts` file contains no Kafka dependency. No `@KafkaListener`, `@KafkaHandler`, or producer configuration exists anywhere in the codebase.

---

## 8. Error Handling

There is no `@ControllerAdvice` or `@ExceptionHandler` class in the codebase. All error responses are produced by Spring's default exception handling mechanism via `ResponseStatusException`.

Errors thrown by service methods:

| Exception | HTTP Status | Message pattern |
|---|---|---|
| `ResponseStatusException(NOT_FOUND)` | `404 Not Found` | `Translation <id> not found` |
| `ResponseStatusException(NOT_FOUND)` | `404 Not Found` | `Language <code> not found` |
| `ResponseStatusException(NOT_FOUND)` | `404 Not Found` | `Job <id> not found` |
| `ResponseStatusException(NOT_FOUND)` | `404 Not Found` | `No translation found for <type>/<id>/<field>` |

**Default error response format** (Spring Boot default `BasicErrorController`):

```json
{
  "timestamp": "2024-10-01T12:00:00.000+00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Translation abc not found",
  "path": "/api/translations/abc"
}
```

Duplicate unique-key violations (e.g. creating a translation that already exists for the same `entity_type/entity_id/field_name/language_code`) are not caught explicitly and will result in a `500 Internal Server Error` from the database constraint.

---

## 9. Configuration and Deployment

### 9.1 Port and Context Path

The service listens on port `8083` with context path `/api`.

Swagger UI is available at `http://localhost:8083/api/swagger-ui.html`.  
OpenAPI JSON is served at `http://localhost:8083/api/v3/api-docs`.

### 9.2 Environment Variables

| Variable | Default | Required | Description |
|---|---|---|---|
| `LOCALIZATION_SERVER_PORT` | `8083` | No | HTTP server port |
| `LOCALIZATION_API_PREFIX` | `/api` | No | Servlet context path |
| `LOCALIZATION_POSTGRES_HOST` | `localhost` | No | PostgreSQL host |
| `LOCALIZATION_POSTGRES_PORT` | `5432` | No | PostgreSQL port |
| `LOCALIZATION_POSTGRES_DB` | — | **Yes** | PostgreSQL database name |
| `LOCALIZATION_POSTGRES_USER` | — | **Yes** | PostgreSQL username |
| `LOCALIZATION_POSTGRES_PASSWORD` | — | **Yes** | PostgreSQL password |
| `LOCALIZATION_HIKARI_POOL_MAX` | `25` | No | HikariCP maximum pool size |
| `LOCALIZATION_HIKARI_POOL_MIN` | `2` | No | HikariCP minimum idle connections |
| `LOCALIZATION_HIKARI_POOL_TIMEOUT_MILLIS` | `30000` | No | HikariCP connection timeout (ms) |
| `LOCALIZATION_POSTGRES_SHOW_SQL` | `true` | No | Log SQL statements to console |
| `LOCALIZATION_POSTGRES_DDL_AUTO` | `update` | No | Hibernate `hbm2ddl.auto` strategy |
| `LOCALIZATION_SEED_ENABLE` | `false` | No | Enable data seeding on startup |
| `LOCALIZATION_SEED_MODE` | `init` | No | Seed dataset subdirectory under `static/data/` |
| `LOCALIZATION_SEED_STOP_COUNT` | `1` | No | Skip seeding if the table already has more rows than this value |

### 9.3 Data Seeding

When `LOCALIZATION_SEED_ENABLE=true`, two `CommandLineRunner` beans execute on startup in order:

1. **`LanguageDataSeed` (Order 1)** — reads `static/data/<mode>/language.json` and inserts `Language` records. Skipped if `languages` table already has more rows than `LOCALIZATION_SEED_STOP_COUNT`.
2. **`TranslationDataSeed` (Order 2)** — reads `static/data/<mode>/translation.json` and inserts `Translation` records with `status = APPROVED`. Skipped by the same stop-count condition. Seeds are bundled for `mode = init`.

### 9.4 Docker

The `Dockerfile` uses a two-stage build:

```
Stage 1: gradle:8.10-jdk21  — builds the fat JAR via ./gradlew :localization-service:bootJar
Stage 2: eclipse-temurin:21-jre-alpine — runs the JAR, exposes port 8083
```

To build and run manually:

```bash
# Copy the sample env file
cp .env.sample .env

# Build the image (from repo root)
docker build -f localization-service/Dockerfile -t localization-service .

# Run the container
docker run --env-file localization-service/.env -p 8083:8083 localization-service
```

To run in development without Docker:

```bash
# Start the database
docker compose up postgres-localization -d

# Run the service
./gradlew :localization-service:bootRun
```

---

## 10. Testing

✅ / ❌ The `spring-boot-starter-test` dependency is present in `build.gradle.kts` and JUnit Platform is configured in `tasks.test`.

However, **no test source files exist**. The directory `src/test/` is absent from the project. There are no unit tests, integration tests, or any other automated test coverage.

---

## 11. Known Limitations and Missing Functionality

### ❌ No translation job execution

`TranslationJobService.create()` saves a job record with status `PENDING` but does not trigger any execution. No scheduler, `@Async` worker, or message producer is implemented. The `TranslationJobRepository.findByStatus()` method is defined but never called. Jobs remain in `PENDING` status indefinitely.

### ❌ No auto-translation integration

The `AUTO_TRANSLATED` status value exists in the `TranslationStatus` enum but no code path sets it. There is no integration with any external translation API (Google Translate, DeepL, etc.).

### ❌ No `languageCode` filter on `GET /api/translations`

The existing README mentions a `languageCode` filter parameter on the list endpoint, but `TranslationController.getAll()` only accepts `entityType` and `entityId`. There is no `languageCode` query parameter in the actual controller or repository.

### ❌ `LanguageService.findByCode()` is unreachable

`LanguageService.findByCode()` is defined but no controller exposes an endpoint to fetch a single language by code. `GET /api/languages/{code}` does not exist.

### ❌ No `GET /api/translations/{id}` endpoint

`TranslationService.findById()` is implemented but no controller handler exposes it. Looking up a single translation by its UUID is not possible via the HTTP API.

### ❌ No conflict response for duplicate translations

Creating a translation with a `(entity_type, entity_id, field_name, language_code)` combination that already exists produces a database constraint violation surfaced as `500 Internal Server Error`. No `409 Conflict` handling is implemented.

### ❌ No test coverage

The `src/test/` directory does not exist. There are no unit or integration tests for any class.

### ⚠️ `Main.kt` is a stub

`src/main/kotlin/Main.kt` contains an IntelliJ-generated placeholder `main()` function that prints "Hello, Kotlin!" and counts to 5. It is not the application entry point (which is `LocalizationServiceApplication.kt`) and has no functional purpose.

### ⚠️ Partial update semantics on `PUT /api/translations/{id}`

The update endpoint accepts `null` for both fields, which means a `PUT` request with an empty body `{}` is accepted silently and only refreshes `updatedAt`. The endpoint behaves more like `PATCH` semantics despite using `PUT`.

### ⚠️ SSL hardcoded to `disable`

The PostgreSQL connection SSL mode is hardcoded to `disable` in `application.yml` (`ssl: disable`) and cannot be changed via environment variables.
