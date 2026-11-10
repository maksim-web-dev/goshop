# api-service

API aggregation gateway for GoShop. Authenticates store clients, enforces per-store rate limits, and aggregates data from seven downstream microservices into single responses.

**Port:** 8080  
**Context path:** `/`  
**No database** — stateless aggregator

---

## Authentication

All `/api/v1/catalog/**` endpoints require an `X-Store-Api-Key` header.  
The key is validated against `store-service` on every request (BCrypt verification).

Exempt paths: `/actuator/**`, `/v3/api-docs/**`, `/swagger-ui/**`

---

## Endpoints

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/catalog/products/{id}` | Aggregated product detail |
| POST | `/api/v1/catalog/products/batch` | Batch product fetch |
| GET | `/api/v1/catalog/search` | Full-text catalog search |
| GET | `/api/v1/catalog/products/{id}/recommendations` | Enriched recommendations |
| POST | `/api/v1/catalog/compatibility/check` | Compatibility check |
| GET | `/api/v1/catalog/categories` | Category list |
| GET | `/actuator/health` | Health (liveness + readiness) |

### GET `/api/v1/catalog/products/{id}`

Query params:
- `lang` — language code (default: store default)
- `currency` — ISO 4217 (default: store default)
- `includeRecommendations` — boolean (default: false)
- `includeAttributes` — boolean (default: true)

### POST `/api/v1/catalog/products/batch`

```json
{
  "productIds": [1001, 1002],
  "lang": "uk",
  "includePrices": true
}
```
Max 100 product IDs.

### GET `/api/v1/catalog/search`

Query params: `q`, `categoryId`, `brand`, `lang`, `page` (default 0), `size` (default 20, max 100)

### GET `/api/v1/catalog/products/{id}/recommendations`

Query params: `type` (SIMILAR|ALTERNATIVES|ACCESSORIES, default SIMILAR), `limit` (1–50, default 10), `lang`

### POST `/api/v1/catalog/compatibility/check`

```json
{ "productIds": [1001, 1005] }
```
Max 50 product IDs.

---

## Downstream Services

Configured via environment variables:

| Variable | Default (local) |
|---|---|
| `PRODUCT_SERVICE_URL` | `http://localhost:8081` |
| `LOCALIZATION_SERVICE_URL` | `http://localhost:8083` |
| `PRICING_SERVICE_URL` | `http://localhost:8084` |
| `COMPATIBILITY_SERVICE_URL` | `http://localhost:8082` |
| `SEARCH_SERVICE_URL` | `http://localhost:8086` |
| `RECOMMENDATIONS_SERVICE_URL` | `http://localhost:8085` |
| `STORE_SERVICE_URL` | `http://localhost:8087` |
| `API_CONNECT_TIMEOUT_MS` | `3000` |
| `API_READ_TIMEOUT_MS` | `10000` |

In Docker Compose, internal DNS names are used (e.g. `http://product-service:8080`).

---

## Rate Limiting

Per-store token bucket rate limiter, seeded from store settings (`requestsPerSecond`, `burstLimit`).  
Returns HTTP 429 when exceeded.

**Note:** In-memory only — does not enforce a global limit across multiple replicas.  
Replace `InMemoryTokenBucketRateLimiter` with a Redis-backed implementation (`RateLimiter` interface) for multi-instance deployments.

---

## Parallel Execution

Product detail and recommendations endpoints execute independent downstream calls concurrently using `CompletableFuture` with Java 21 virtual threads.

Optional dependencies (recommendations, translations) fail silently and produce a `warnings` field in the response rather than a 5xx error.

---

## Build

```bash
./gradlew :api-service:bootJar
```

## Docker

```bash
docker build -f api-service/Dockerfile -t api-service .
```
