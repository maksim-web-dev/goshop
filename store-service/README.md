# store-service

## 1. Service Overview

`store-service` is the microservice responsible for managing tenant stores on the GoShop platform. Its scope covers four first-class concerns:

- **Store management** — registration, retrieval, partial updates, and soft-deactivation of stores. Each store has a unique domain and carries its own country, language, and currency defaults.
- **API key issuance** — generating secure API keys for a store. A 32-byte cryptographically random value is generated with `SecureRandom`, formatted as a hex string with an `sk_live_` prefix, and **returned to the caller only once**. Only a BCrypt hash (`keyHash`) and the first 16 characters (`keyPrefix`) are persisted in the database. The plaintext key is never stored.
- **Rate-limit settings** — each store owns a `StoreSettings` record that declares `requestsPerSecond`, `burstLimit`, and `allowedLanguages`. Settings are created with defaults on first access if they do not yet exist.
- **Webhook subscriptions** — stores can register webhook endpoints with a list of event-type strings and a reference to an external secret (Vault / Secrets Manager). The secret value itself is never stored in the database.

**Data ownership:** this service is the single source of truth for the `stores`, `store_api_keys`, `store_settings`, `store_allowed_languages`, `store_webhooks`, and `store_webhook_event_types` PostgreSQL tables.

---

## 2. Technology Stack

| Component | Version / Artifact |
|---|---|
| Kotlin | 2.0.0 |
| JVM toolchain | 21 |
| Spring Boot | 3.3.5 |
| Spring Dependency Management plugin | 1.1.6 |
| `spring-boot-starter-web` | (managed by Boot 3.3.5) |
| `spring-boot-starter-data-jpa` | (managed by Boot 3.3.5) |
| `spring-boot-starter-validation` | (managed by Boot 3.3.5) |
| `spring-boot-starter-security` | (managed by Boot 3.3.5) |
| `jackson-module-kotlin` | (managed by Boot 3.3.5) |
| `springdoc-openapi-starter-webmvc-ui` | 2.6.0 |
| `org.postgresql:postgresql` | (managed by Boot 3.3.5, runtime) |
| `spring-boot-starter-test` | (managed by Boot 3.3.5, test) |
| HikariCP | (transitive via spring-boot-starter-data-jpa) |
| Hibernate / PostgreSQL dialect | (transitive via spring-boot-starter-data-jpa) |

**Security note:** `BCryptPasswordEncoder` (Spring Security) and `java.security.SecureRandom` are used together for API key generation. Spring Security itself is configured in stateless, fully-open mode (no authentication enforced by this service).

---

## 3. Database Model

### 3.1 Enum: `StoreStatus`

| Value | Meaning |
|---|---|
| `ACTIVE` | Store is operational |
| `INACTIVE` | Store has been deactivated (set by the `DELETE /stores/{id}` endpoint) |
| `SUSPENDED` | Reserved value; not set by any current service logic |

### 3.2 Enum: `ApiKeyStatus`

| Value | Meaning |
|---|---|
| `ACTIVE` | Key is valid |
| `REVOKED` | Key has been explicitly revoked |
| `EXPIRED` | Reserved value; expiry is not enforced by any current service logic |

### 3.3 Entity: `Store` (table `stores`)

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `id` | `Long` | PK, auto-generated (`GenerationType.IDENTITY`) | |
| `name` | `VARCHAR` | NOT NULL | Display name |
| `domain` | `VARCHAR` | NOT NULL, UNIQUE | Tenant domain |
| `country_code` | `VARCHAR(2)` | NOT NULL | ISO 3166-1 alpha-2 |
| `default_language` | `VARCHAR(10)` | NOT NULL | BCP 47 |
| `default_currency` | `VARCHAR(3)` | NOT NULL | ISO 4217 |
| `status` | `VARCHAR` (enum) | NOT NULL | Default: `ACTIVE` |
| `created_at` | `TIMESTAMP` | NOT NULL, not updatable | Set at creation |

**Indexes:** `idx_stores_status` (`status`). `domain` has a column-level `UNIQUE` constraint (implicit index).

### 3.4 Entity: `StoreApiKey` (table `store_api_keys`)

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `id` | `Long` | PK, auto-generated (`GenerationType.IDENTITY`) | |
| `store_id` | `Long` | FK → `stores.id`, NOT NULL | `@ManyToOne LAZY` |
| `key_hash` | `VARCHAR` | NOT NULL | BCrypt hash of the full plaintext key |
| `key_prefix` | `VARCHAR(16)` | NOT NULL | First 16 characters of the plaintext key (e.g. `sk_live_ab12cd34`) |
| `status` | `VARCHAR` (enum) | NOT NULL | Default: `ACTIVE` |
| `created_at` | `TIMESTAMP` | NOT NULL, not updatable | |
| `expires_at` | `TIMESTAMP` | nullable | Optional expiry; not enforced at runtime |
| `last_used_at` | `TIMESTAMP` | nullable | Not updated by any current logic |

**Indexes:** `idx_store_api_keys_store_id` (`store_id`), `idx_store_api_keys_key_prefix_status` (`key_prefix, status`).

### 3.5 Entity: `StoreSettings` (table `store_settings`)

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `id` | `Long` | PK, auto-generated (`GenerationType.IDENTITY`) | |
| `store_id` | `Long` | FK → `stores.id`, NOT NULL, UNIQUE | `@OneToOne LAZY` |
| `requests_per_second` | `INT` | NOT NULL | Default: 100 |
| `burst_limit` | `INT` | NOT NULL | Default: 200 |

**Collection table `store_allowed_languages`** (`@ElementCollection`):

| Column | Type | Notes |
|---|---|---|
| `store_settings_id` | `Long` | FK → `store_settings.id` |
| `language_code` | `VARCHAR(10)` | BCP 47 code (e.g. `"uk"`, `"en"`) |

The `store_id` FK has a column-level `UNIQUE` constraint (implicit index).

### 3.6 Entity: `StoreWebhook` (table `store_webhooks`)

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `id` | `Long` | PK, auto-generated (`GenerationType.IDENTITY`) | |
| `store_id` | `Long` | FK → `stores.id`, NOT NULL | `@ManyToOne LAZY` |
| `url` | `VARCHAR` | NOT NULL | Delivery target URL |
| `secret_ref` | `VARCHAR` | NOT NULL | Reference to external secret; actual secret not stored |
| `enabled` | `BOOLEAN` | NOT NULL | Default: `true` |

**Indexes:** `idx_store_webhooks_store_id` (`store_id`).

**Collection table `store_webhook_event_types`** (`@ElementCollection`):

| Column | Type | Notes |
|---|---|---|
| `webhook_id` | `Long` | FK → `store_webhooks.id` |
| `event_type` | `VARCHAR` | Free-form string, e.g. `"product.created"`, `"price.updated"` |

---

## 4. REST API Endpoints

Base URL: `http://localhost:8087/api`

OpenAPI UI: `http://localhost:8087/api/swagger-ui.html`

---

### 4.1 `GET /api/stores`

**Controller:** `StoreController.getAll()`  
**Purpose:** Returns all registered stores.

**Response:** `200 OK` — array of `StoreResponse`

**`StoreResponse` fields:**

| Field | Type | Notes |
|---|---|---|
| `id` | `Long` | |
| `name` | `String` | |
| `domain` | `String` | |
| `countryCode` | `String` | |
| `defaultLanguage` | `String` | |
| `defaultCurrency` | `String` | |
| `status` | `StoreStatus` | `ACTIVE` \| `INACTIVE` \| `SUSPENDED` |
| `createdAt` | `String` (ISO-8601) | |

**Example response:**
```json
[
  {
    "id": 1,
    "name": "GoShop Main",
    "domain": "goshop.com",
    "countryCode": "US",
    "defaultLanguage": "en",
    "defaultCurrency": "USD",
    "status": "ACTIVE",
    "createdAt": "2024-01-15T10:30:00Z"
  }
]
```

---

### 4.2 `GET /api/stores/{id}`

**Controller:** `StoreController.getById()`  
**Purpose:** Returns a single store by its ID.

**Path variables:**

| Variable | Type | Required | Notes |
|---|---|---|---|
| `id` | `Long` | yes | Store identifier |

**Response:** `200 OK` — single `StoreResponse`

**Possible errors:**

| Status | Condition |
|---|---|
| `404 Not Found` | No store with the given `id` exists |

---

### 4.3 `POST /api/stores`

**Controller:** `StoreController.create()`  
**Purpose:** Registers a new store. The domain must be globally unique.

**Request body:** `StoreCreateRequest`

| Field | Type | Required | Constraints | Notes |
|---|---|---|---|---|
| `name` | `String` | yes | 1–255 chars | Display name |
| `domain` | `String` | yes | 1–255 chars, unique | Tenant domain |
| `countryCode` | `String` | yes | exactly 2 chars | ISO 3166-1 alpha-2, e.g. `"US"` |
| `defaultLanguage` | `String` | yes | 2–10 chars | BCP 47, e.g. `"en"` |
| `defaultCurrency` | `String` | yes | exactly 3 chars | ISO 4217, e.g. `"USD"` |

**Response:** `201 Created` — `StoreResponse`

**Possible errors:**

| Status | Condition |
|---|---|
| `400 Bad Request` | Blank fields or wrong code lengths |
| `409 Conflict` | The `domain` is already registered |

**Example request:**
```json
{
  "name": "My Shop",
  "domain": "myshop.com",
  "countryCode": "US",
  "defaultLanguage": "en",
  "defaultCurrency": "USD"
}
```

**Example response:**
```json
{
  "id": 2,
  "name": "My Shop",
  "domain": "myshop.com",
  "countryCode": "US",
  "defaultLanguage": "en",
  "defaultCurrency": "USD",
  "status": "ACTIVE",
  "createdAt": "2024-06-01T12:00:00Z"
}
```

---

### 4.4 `PATCH /api/stores/{id}`

**Controller:** `StoreController.update()`  
**Purpose:** Partially updates a store. `domain` and `countryCode` cannot be changed via this endpoint.

**Path variables:**

| Variable | Type | Required |
|---|---|---|
| `id` | `Long` | yes |

**Request body:** `StoreUpdateRequest` — all fields are optional

| Field | Type | Required | Constraints | Notes |
|---|---|---|---|---|
| `name` | `String?` | no | 1–255 chars if provided | New display name |
| `defaultLanguage` | `String?` | no | 2–10 chars if provided | New BCP 47 language code |
| `defaultCurrency` | `String?` | no | exactly 3 chars if provided | New ISO 4217 currency code |
| `status` | `StoreStatus?` | no | | `ACTIVE`, `INACTIVE`, or `SUSPENDED` |

**Response:** `200 OK` — `StoreResponse`

**Possible errors:**

| Status | Condition |
|---|---|
| `400 Bad Request` | Wrong code lengths |
| `404 Not Found` | No store with the given `id` exists |

**Example request:**
```json
{
  "defaultCurrency": "EUR",
  "status": "INACTIVE"
}
```

---

### 4.5 `DELETE /api/stores/{id}`

**Controller:** `StoreController.deactivate()`  
**Purpose:** Soft-deactivates a store by setting its status to `INACTIVE`. The record is not deleted.

**Path variables:**

| Variable | Type | Required |
|---|---|---|
| `id` | `Long` | yes |

**Response:** `204 No Content`

**Possible errors:**

| Status | Condition |
|---|---|
| `404 Not Found` | No store with the given `id` exists |

---

### 4.6 `POST /api/stores/{storeId}/api-keys`

**Controller:** `StoreApiKeyController.create()`  
**Purpose:** Generates a new API key for the specified store. The full plaintext key is returned **only in this response** and is never retrievable again. Only the BCrypt hash is persisted.

**Path variables:**

| Variable | Type | Required |
|---|---|---|
| `storeId` | `Long` | yes |

**Request body:** `StoreApiKeyCreateRequest`

| Field | Type | Required | Notes |
|---|---|---|---|
| `expiresAt` | `String?` (ISO-8601 `Instant`) | no | Optional expiry timestamp; `null` means no expiry |

**Response:** `201 Created` — `StoreApiKeyCreateResponse`

| Field | Type | Notes |
|---|---|---|
| `id` | `Long` | Key record identifier |
| `keyPrefix` | `String` | First 16 characters of the key, e.g. `"sk_live_ab12cd34"` |
| `plainKey` | `String` | **Full plaintext key — shown only once.** Format: `sk_live_` + 64 hex chars |
| `status` | `ApiKeyStatus` | Always `ACTIVE` on creation |
| `createdAt` | `String` (ISO-8601) | |
| `expiresAt` | `String?` (ISO-8601) | `null` if no expiry was set |

**Possible errors:**

| Status | Condition |
|---|---|
| `404 Not Found` | No store with the given `storeId` exists |

**Example request:**
```json
{
  "expiresAt": "2025-12-31T23:59:59Z"
}
```

**Example response** (key masked after the prefix for illustration):
```json
{
  "id": 1,
  "keyPrefix": "sk_live_ab12cd34",
  "plainKey": "sk_live_ab12cd34ef56gh78****************************",
  "status": "ACTIVE",
  "createdAt": "2024-06-01T12:00:00Z",
  "expiresAt": "2025-12-31T23:59:59Z"
}
```

---

### 4.7 `POST /api/stores/{storeId}/api-keys/{keyId}/revoke`

**Controller:** `StoreApiKeyController.revoke()`  
**Purpose:** Revokes an API key by setting its status to `REVOKED`. The key must belong to the specified store.

**Path variables:**

| Variable | Type | Required |
|---|---|---|
| `storeId` | `Long` | yes |
| `keyId` | `Long` | yes |

**Response:** `204 No Content`

**Possible errors:**

| Status | Condition |
|---|---|
| `404 Not Found` | No key with the given `keyId` exists |
| `403 Forbidden` | The key exists but belongs to a different store |

---

### 4.8 `GET /api/stores/{storeId}/settings`

**Controller:** `StoreSettingsController.get()`  
**Purpose:** Returns the rate-limit and language settings for a store. If no settings record exists yet, one is created with default values (`requestsPerSecond=100`, `burstLimit=200`, empty `allowedLanguages`).

**Path variables:**

| Variable | Type | Required |
|---|---|---|
| `storeId` | `Long` | yes |

**Response:** `200 OK` — `StoreSettingsResponse`

| Field | Type | Notes |
|---|---|---|
| `id` | `Long` | Settings record identifier |
| `storeId` | `Long` | Owning store identifier |
| `requestsPerSecond` | `Int` | |
| `burstLimit` | `Int` | |
| `allowedLanguages` | `List<String>` | BCP 47 codes |

**Possible errors:**

| Status | Condition |
|---|---|
| `404 Not Found` | No store with the given `storeId` exists (triggered during lazy-create) |

**Example response:**
```json
{
  "id": 1,
  "storeId": 1,
  "requestsPerSecond": 100,
  "burstLimit": 200,
  "allowedLanguages": ["en", "uk"]
}
```

---

### 4.9 `PUT /api/stores/{storeId}/settings`

**Controller:** `StoreSettingsController.update()`  
**Purpose:** Replaces rate-limit settings for a store. All fields are optional; omitted fields retain their current values. If no settings record exists, one is created (lazy-create) before applying the update.

**Path variables:**

| Variable | Type | Required |
|---|---|---|
| `storeId` | `Long` | yes |

**Request body:** `StoreSettingsUpdateRequest` — all fields optional

| Field | Type | Required | Constraints | Notes |
|---|---|---|---|---|
| `requestsPerSecond` | `Int?` | no | > 0 if provided | New requests-per-second limit |
| `burstLimit` | `Int?` | no | > 0 if provided | New burst limit |
| `allowedLanguages` | `List<String>?` | no | | Replaces the entire list of allowed language codes |

**Response:** `200 OK` — `StoreSettingsResponse`

**Possible errors:**

| Status | Condition |
|---|---|
| `400 Bad Request` | Non-positive rate limit values |
| `404 Not Found` | No store with the given `storeId` exists |

**Example request:**
```json
{
  "requestsPerSecond": 50,
  "burstLimit": 100,
  "allowedLanguages": ["en", "de", "fr"]
}
```

---

### 4.10 `POST /api/stores/{storeId}/webhooks`

**Controller:** `StoreWebhookController.create()`  
**Purpose:** Registers a new webhook endpoint for a store.

**Path variables:**

| Variable | Type | Required |
|---|---|---|
| `storeId` | `Long` | yes |

**Request body:** `StoreWebhookCreateRequest`

| Field | Type | Required | Constraints | Notes |
|---|---|---|---|---|
| `url` | `String` | yes | not blank | HTTPS endpoint that receives events |
| `eventTypes` | `List<String>` | yes | not empty | Event type strings, e.g. `["product.created", "price.updated"]` |
| `secretRef` | `String` | yes | not blank | Reference path to signing secret in Vault/Secrets Manager |
| `enabled` | `Boolean` | no | | Default: `true` |

**Response:** `201 Created` — `StoreWebhookResponse`

| Field | Type | Notes |
|---|---|---|
| `id` | `Long` | Webhook record identifier |
| `storeId` | `Long` | Owning store identifier |
| `url` | `String` | |
| `eventTypes` | `List<String>` | |
| `secretRef` | `String` | |
| `enabled` | `Boolean` | |

**Possible errors:**

| Status | Condition |
|---|---|
| `400 Bad Request` | Blank url/secretRef or empty eventTypes |
| `404 Not Found` | No store with the given `storeId` exists |

**Example request:**
```json
{
  "url": "https://myshop.com/hooks/goshop",
  "eventTypes": ["product.created", "order.placed"],
  "secretRef": "secret/goshop/myshop/webhook",
  "enabled": true
}
```

**Example response:**
```json
{
  "id": 1,
  "storeId": 1,
  "url": "https://myshop.com/hooks/goshop",
  "eventTypes": ["product.created", "order.placed"],
  "secretRef": "secret/goshop/myshop/webhook",
  "enabled": true
}
```

---

## 5. Business Logic

### 5.1 `StoreApiKeyService` — API key generation

```
1. Retrieve the store by storeId (404 if absent).
2. Generate 32 random bytes via SecureRandom.nextBytes().
3. Format them as lowercase hex with java.util.HexFormat.of().formatHex().
4. Prepend "sk_live_" → plainKey (total length: 8 + 64 = 72 chars).
5. keyPrefix = plainKey.take(16)   // "sk_live_" + first 8 hex chars
6. keyHash   = BCryptPasswordEncoder().encode(plainKey)
7. Persist StoreApiKey(store, keyHash, keyPrefix, expiresAt).
8. Return CreatedApiKey(entity, plainKey).
```

`CreatedApiKey` is a `data class` defined in the same file as `StoreApiKeyService`:
```kotlin
data class CreatedApiKey(val entity: StoreApiKey, val plainKey: String)
```

The `BCryptPasswordEncoder` instance and `SecureRandom` instance are created once at service construction and reused across calls.

The repository also exposes `findByKeyPrefixAndStatus(keyPrefix, status)` which could support a future key-validation lookup, but no endpoint currently calls it.

### 5.2 `StoreSettingsService` — lazy-create default settings

`findByStore(storeId)` checks `StoreSettingsRepository.findByStoreId()`. If `null` is returned, it fetches the store from `StoreRepository` (throwing `404` if absent) and immediately persists a new `StoreSettings` record with default values (`requestsPerSecond=100`, `burstLimit=200`, `allowedLanguages=[]`). Both `GET` and `PUT` settings endpoints call `findByStore`, so the first access always guarantees a record exists.

### 5.3 `StoreService` — domain uniqueness and deactivation

- `create`: checks `findByDomain(dto.domain)` before saving; throws `409 Conflict` if the domain is already taken.
- `update`: only `name`, `defaultLanguage`, `defaultCurrency`, and `status` can be changed. `domain` and `countryCode` are immutable once set.
- `deactivate`: sets `status = INACTIVE` by saving a new `Store` instance with the same ID and `INACTIVE` status. The row is not deleted.

---

## 6. Inter-Service Communication

No inter-service HTTP communication is currently implemented. The service operates exclusively on its own PostgreSQL database and does not make HTTP calls to any other microservice.

---

## 7. Kafka and Asynchronous Events

Kafka is not implemented. There is no Kafka dependency in `build.gradle.kts` and no producer or consumer code in the service.

---

## 8. Error Handling

`GlobalExceptionHandler` (`@RestControllerAdvice`) provides consistent JSON error responses:

| Exception | HTTP Status | Notes |
|---|---|---|
| `MethodArgumentNotValidException` | `400 Bad Request` | `@RequestBody` validation failures; response includes `details` array with field-level messages |
| `ResponseStatusException` | varies | Thrown explicitly by service methods (e.g. `404`, `409`, `403`) |
| `Exception` (catch-all) | `500 Internal Server Error` | Unexpected errors |

**Validation error response format:**
```json
{
  "status": 400,
  "error": "Validation failed",
  "details": [
    "countryCode: size must be between 2 and 2",
    "defaultCurrency: size must be between 3 and 3"
  ]
}
```

**HTTP status mapping:**

| Exception trigger | HTTP Status |
|---|---|
| Store / key not found by ID | `404 Not Found` |
| Domain already registered | `409 Conflict` |
| Key belongs to a different store (revoke) | `403 Forbidden` |
| Spring Security is stateless, all requests permitted | No `401`/`403` from security layer |

---

## 9. Configuration and Deployment

### 9.1 Application configuration

`server.port` defaults to `8087`; `server.servlet.context-path` defaults to `/api`.

| Environment variable | Default | Required | Description |
|---|---|---|---|
| `STORE_SERVER_PORT` | `8087` | no | HTTP listen port |
| `STORE_API_PREFIX` | `/api` | no | Servlet context path (URL prefix for all endpoints) |
| `STORE_POSTGRES_HOST` | `localhost` | no | PostgreSQL hostname |
| `STORE_POSTGRES_PORT` | `5432` | no | PostgreSQL port |
| `STORE_POSTGRES_DB` | — | **yes** | Database name |
| `STORE_POSTGRES_USER` | — | **yes** | Database username |
| `STORE_POSTGRES_PASSWORD` | — | **yes** | Database password |
| `STORE_POSTGRES_DDL_AUTO` | `update` | no | Hibernate DDL strategy (`none`, `validate`, `update`, `create`, `create-drop`) |
| `STORE_POSTGRES_SHOW_SQL` | `true` | no | Log SQL statements |
| `STORE_HIKARI_POOL_MAX` | `25` | no | HikariCP maximum pool size |
| `STORE_HIKARI_POOL_MIN` | `2` | no | HikariCP minimum idle connections |
| `STORE_HIKARI_POOL_TIMEOUT_MILLIS` | `30000` | no | HikariCP connection timeout (ms) |
| `STORE_SEED_ENABLE` | `false` | no | Enable data seeding on startup |
| `STORE_SEED_MODE` | `init` | no | Seed mode subdirectory under `static/data/` |
| `STORE_SEED_STOP_COUNT` | `1` | no | Skip seeding if the target table already has more than this many rows |

### 9.2 Data seeding

When `STORE_SEED_ENABLE=true`, three `CommandLineRunner` beans execute in order:

1. `StoreDataSeed` (Order 1) — loads `static/data/{mode}/store.json`
2. `StoreSettingsDataSeed` (Order 2) — loads `static/data/{mode}/store_settings.json`
3. `StoreWebhookDataSeed` (Order 3) — loads `static/data/{mode}/store_webhook.json`

Each runner checks the row count in its table against `STORE_SEED_STOP_COUNT` before inserting, so re-running the application does not duplicate data.

### 9.3 Docker

**Dockerfile** — multi-stage build:
- Builder stage: `gradle:8.10-jdk21`, runs `./gradlew :store-service:bootJar -x test`
- Runtime stage: `eclipse-temurin:21-jre-alpine`, exposes port `8087`

**docker-compose.yml** entry (excerpt):

```yaml
store-service:
  build:
    context: .
    dockerfile: store-service/Dockerfile
  container_name: store-service
  ports:
    - "8087:8087"
  environment:
    STORE_POSTGRES_HOST: postgres-store
    STORE_POSTGRES_PORT: 5432
    STORE_POSTGRES_DB: store_db
    STORE_POSTGRES_USER: postgres
    STORE_POSTGRES_PASSWORD: postgres
    STORE_POSTGRES_DDL_AUTO: update
    STORE_POSTGRES_SHOW_SQL: "false"
    STORE_SEED_ENABLE: "false"
  depends_on:
    postgres-store:
      condition: service_healthy

postgres-store:
  image: postgres:16-alpine
  container_name: postgres-store
  ports:
    - "5438:5432"
  environment:
    POSTGRES_DB: store_db
    POSTGRES_USER: postgres
    POSTGRES_PASSWORD: postgres
```

### 9.4 Local startup

```bash
# Copy and fill in credentials
cp store-service/.env.sample store-service/.env

# Start the dedicated PostgreSQL instance
docker compose up postgres-store -d

# Run the service
./gradlew :store-service:bootRun
```

OpenAPI docs: `http://localhost:8087/api/swagger-ui.html`

---

## 10. Testing

No test files exist under `src/test/`. The `build.gradle.kts` includes `spring-boot-starter-test` as a test dependency and configures JUnit Platform, but no test classes have been written.

---

## 11. Known Limitations and Missing Functionality

| Area | Status | Detail |
|---|---|---|
| API key validation endpoint | ❌ NOT IMPLEMENTED | `StoreApiKeyRepository.findByKeyPrefixAndStatus()` exists and is usable, but no `POST /stores/{id}/api-keys/validate` or equivalent endpoint is implemented. Other services cannot currently verify an API key against this service. |
| Rate-limit enforcement | ❌ NOT IMPLEMENTED | `StoreSettings` stores `requestsPerSecond` and `burstLimit`, but no request-throttling filter, interceptor, or integration with an API gateway exists in the service. The fields are configuration-only. |
| Webhook delivery | ❌ NOT IMPLEMENTED | `StoreWebhook` records are stored with their event subscriptions, but no event publisher, HTTP dispatcher, or retry mechanism exists. Webhooks are never triggered by any action in the service. |
| API key listing | ❌ NOT IMPLEMENTED | `StoreApiKeyRepository.findByStoreId()` is available, but no `GET /stores/{storeId}/api-keys` endpoint is exposed. |
| Webhook listing / deletion | ❌ NOT IMPLEMENTED | `StoreWebhookRepository.findByStoreId()` is available, but no `GET` or `DELETE` endpoint for webhooks is implemented. |
| Expiry enforcement | ❌ NOT IMPLEMENTED | `StoreApiKey.expiresAt` is stored and accepted as input, but no scheduler or request-time check compares it against the current time. `ApiKeyStatus.EXPIRED` is never set by any logic. |
| `lastUsedAt` tracking | ❌ NOT IMPLEMENTED | The column exists on `StoreApiKey` but is never updated. |
| `StoreStatus.SUSPENDED` | ⚠️ PARTIALLY IMPLEMENTED | The enum value exists and can be set via `PATCH /stores/{id}` with `"status": "SUSPENDED"`, but no business logic is attached to this state. |
| Authentication / authorization | ❌ NOT IMPLEMENTED | Spring Security is included but configured to permit all requests without authentication. There is no JWT validation, API-key-based auth, or role enforcement. |
| Test coverage | ❌ NOT IMPLEMENTED | No unit or integration tests exist. |
