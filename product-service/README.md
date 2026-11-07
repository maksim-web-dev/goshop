# product-service

## 1. Service Overview

`product-service` is the authoritative catalog microservice in the GoShop platform. It owns and manages every entity that describes **what** can be sold.

**Responsibilities:**
- Product lifecycle management (create, update, archive)
- Product catalog organization via hierarchical categories and brands
- Product typing via `ProductType`, which scopes the attribute schema for a product
- Attribute schema definition via `CategoryParameter` per category
- Per-product attribute values via `ProductAttribute` (key-value store)
- Reference data for filterable/configurable product properties: `ParameterGroup` / `ParameterGroupItem`
- Reference data for product bill-of-materials: `ComponentGroup` / `Component`

**Implemented capabilities:**
- ✅ Full CRUD for Brand, Category, ProductType, ParameterGroup, Component / ComponentGroup
- ✅ Create / update / archive for Product (delete is soft: sets status to `ARCHIVED`)
- ✅ Paginated, filterable product listing (by `categoryId`, `status`) — max page size: 100
- ✅ Batch product retrieval (`POST /products/batch`, max 100 IDs, single JOIN FETCH query)
- ✅ Detailed product endpoint (`GET /products/{id}/details`) — product + brand + category + type + attributes, 2 queries
- ✅ Atomic replace of all product attributes in a single PUT call
- ✅ Attribute schema lookup for a ProductType (`GET /product-types/{id}/attributes`)
- ✅ Bean Validation on all write endpoints (`@Valid`, `@NotBlank`, `@Positive`, `@Size`)
- ✅ `GlobalExceptionHandler` with structured error responses (400, 404, 409, 500)
- ✅ Database seed mechanism (controlled by env variables, runs on startup)
- ✅ OpenAPI / Swagger UI via springdoc

**Data ownership:** This service is the single source of truth for all catalog entities listed above. No other service modifies these tables.

**Service boundaries:** No outbound HTTP calls to other microservices are made.

---

## 2. Technology Stack

| Component | Version |
|---|---|
| Kotlin (JVM) | 2.2.0 |
| Kotlin Spring plugin | 2.0.0 |
| Kotlin JPA plugin | 2.0.0 |
| Spring Boot | 3.3.5 |
| Spring Dependency Management plugin | 1.1.6 |
| JVM toolchain | 21 (Eclipse Temurin 21-jre-alpine in Docker) |
| Gradle wrapper | 8.10 |

**Runtime dependencies (from `build.gradle.kts`):**

| Dependency | Version | Purpose |
|---|---|---|
| `spring-boot-starter-web` | managed by Boot 3.3.5 | REST controllers (Spring MVC) |
| `spring-boot-starter-data-jpa` | managed by Boot 3.3.5 | JPA / Hibernate ORM |
| `spring-boot-starter-security` | managed by Boot 3.3.5 | Security filter chain (all requests permitted, CSRF disabled) |
| `spring-boot-starter-validation` | managed by Boot 3.3.5 | Jakarta Bean Validation (`@Valid`, `@NotBlank`, `@Positive`, `@Size`) |
| `jackson-module-kotlin` | managed by Boot 3.3.5 | Kotlin-aware JSON serialization |
| `springdoc-openapi-starter-webmvc-ui` | 2.6.0 | Swagger UI + OpenAPI 3 spec generation |
| `postgresql` (runtimeOnly) | managed by Boot 3.3.5 | PostgreSQL JDBC driver |

**Test dependencies:**

| Dependency | Purpose |
|---|---|
| `spring-boot-starter-test` | JUnit 5 platform (no test classes exist yet) |

---

## 3. Database Model

All primary keys use `GenerationType.IDENTITY` (PostgreSQL `BIGSERIAL` / auto-increment). There are no UUID primary keys in this service.

### Entity Definitions

#### `brands`
| Column | Kotlin Type | Constraints |
|---|---|---|
| `id` | `Long` | PK, auto-increment |
| `name` | `String` | NOT NULL, UNIQUE |
| `website_url` | `String?` | nullable |

#### `categories`
| Column | Kotlin Type | Constraints |
|---|---|---|
| `id` | `Long` | PK, auto-increment |
| `parent_id` | `Long?` | FK → `categories.id`, nullable (self-referential, LAZY) |
| `name` | `String` | NOT NULL |
| `slug` | `String` | NOT NULL, UNIQUE |

#### `product_types`
| Column | Kotlin Type | Constraints |
|---|---|---|
| `id` | `Long` | PK, auto-increment |
| `category_id` | `Long` | FK → `categories.id`, NOT NULL, LAZY |
| `name` | `String` | NOT NULL |

#### `parameter_groups`
| Column | Kotlin Type | Constraints |
|---|---|---|
| `id` | `Long` | PK, auto-increment |
| `name` | `String` | NOT NULL |

#### `parameter_group_items`
| Column | Kotlin Type | Constraints |
|---|---|---|
| `id` | `Long` | PK, auto-increment |
| `name` | `String` | NOT NULL |
| `parameter_group_id` | `Long` | FK → `parameter_groups.id`, NOT NULL, LAZY |

#### `component_groups`
| Column | Kotlin Type | Constraints |
|---|---|---|
| `id` | `Long` | PK, auto-increment |
| `name` | `String` | NOT NULL |

#### `components`
| Column | Kotlin Type | Constraints |
|---|---|---|
| `id` | `Long` | PK, auto-increment |
| `name` | `String` | NOT NULL |
| `component_group_id` | `Long` | FK → `component_groups.id`, NOT NULL, LAZY |

#### `category_parameters`
| Column | Kotlin Type | Constraints |
|---|---|---|
| `id` | `Long` | PK, auto-increment |
| `name` | `String` | NOT NULL |
| `code` | `String?` | nullable — optional machine-readable identifier (e.g. `"storage_gb"`) |
| `sort_order` | `Int` | NOT NULL, default 0 |
| `category_id` | `Long` | FK → `categories.id`, NOT NULL, LAZY |
| `parameter_type` | `String` (enum) | NOT NULL — values: `INT`, `DOUBLE_2`, `DOUBLE_3`, `STRING`, `BOOLEAN`, `LIST` |
| `int_value` | `Int?` | nullable — used when `parameter_type = INT` |
| `double_value2` | `Int?` | nullable — integer part for `DOUBLE_2` (fractional part in `double_value3`) |
| `double_value3` | `Int?` | nullable — fractional part for `DOUBLE_3` |
| `boolean_value` | `Boolean?` | nullable — used when `parameter_type = BOOLEAN` |
| `string_value` | `String?` | nullable — used when `parameter_type = STRING` |
| `parameter_group_id` | `Long?` | FK → `parameter_groups.id`, nullable — used when `parameter_type = LIST` |

#### `products`
| Column | Kotlin Type | Constraints |
|---|---|---|
| `id` | `Long` | PK, auto-increment |
| `sku` | `String` | NOT NULL, UNIQUE (index via unique constraint) |
| `name` | `String` | NOT NULL |
| `brand_id` | `Long` | FK → `brands.id`, NOT NULL, LAZY |
| `category_id` | `Long` | FK → `categories.id`, NOT NULL, LAZY |
| `product_type_id` | `Long` | FK → `product_types.id`, NOT NULL, LAZY |
| `description` | `String?` | nullable, `TEXT` column |
| `status` | `String` (enum) | NOT NULL, default `DRAFT` — values: `DRAFT`, `ACTIVE`, `ARCHIVED` |
| `created_at` | `Instant` | NOT NULL, not updatable |
| `updated_at` | `Instant` | NOT NULL, mutable |

**Indexes on `products`:**
| Index name | Columns | Purpose |
|---|---|---|
| `idx_products_category_id` | `category_id` | filter by category |
| `idx_products_product_type_id` | `product_type_id` | filter by product type |
| `idx_products_status` | `status` | filter by status |

**Hibernate `@BatchSize(size=25)`** is applied to `Product`, `Brand`, `Category`, and `ProductType` entities to avoid N+1 SELECT queries when loading paginated product lists.

#### `product_attributes`
| Column | Kotlin Type | Constraints |
|---|---|---|
| `id` | `Long` | PK, auto-increment |
| `product_id` | `Long` | FK → `products.id`, NOT NULL, LAZY |
| `category_parameter_id` | `Long` | FK → `category_parameters.id`, NOT NULL, LAZY |
| `value` | `String` | NOT NULL, `TEXT` column |

**Unique constraint:** `(product_id, category_parameter_id)` — a product may have at most one value per category parameter.

### Entity Relationship Diagram

```
parameter_groups ──< parameter_group_items

component_groups ──< components

categories (self-ref, parent_id)
    │
    ├──< product_types
    │       │
    │       └──< products >── brands
    │                │
    │                └──< product_attributes
    │                             │
    └──< category_parameters ─────┘
              │
              └──> parameter_groups  (optional, for LIST type)
```

### Enum Types

**`ProductStatus`** (`com.data.entity.ProductStatus`):
| Value | Meaning |
|---|---|
| `DRAFT` | Newly created, not publicly visible (default on creation) |
| `ACTIVE` | Live / published |
| `ARCHIVED` | Soft-deleted via `DELETE /products/{id}` |

**`ParameterType`** (`com.data.entity.ParameterType`):
| Value | Meaning |
|---|---|
| `INT` | Integer value stored in `int_value` |
| `DOUBLE_2` | 2-decimal float — integer part in `int_value` (or `double_value2`), fractional in `double_value3` |
| `DOUBLE_3` | 3-decimal float — integer part in `double_value2`, fractional in `double_value3` |
| `STRING` | Free text, stored in `string_value` |
| `BOOLEAN` | Boolean flag, stored in `boolean_value` |
| `LIST` | Enumerated values; references a `ParameterGroup` via `parameter_group_id` |

---

## 4. REST API Endpoints

Base URL prefix: `/api` (configured via `server.servlet.context-path`).

All requests and responses use `Content-Type: application/json`. Jackson is configured with `write-dates-as-timestamps: false`, so `Instant` fields are serialized as ISO-8601 strings.

---

### Brands — `BrandController` (`/brands`)

#### `GET /api/brands`
Returns all brands.

- **Handler:** `BrandController.getAll()`
- **Response:** `200 OK` — `List<BrandResponse>`

**Response body:**
```json
[
  { "id": 1, "name": "Samsung", "websiteUrl": "https://samsung.com" },
  { "id": 2, "name": "Apple",   "websiteUrl": null }
]
```

---

#### `GET /api/brands/{id}`
Returns a single brand by ID.

- **Handler:** `BrandController.getById(id: Long)`
- **Path variable:** `id` — `Long`
- **Response:** `200 OK` — `BrandResponse`
- **Errors:** `404 Not Found` if brand does not exist

**Response body:**
```json
{ "id": 1, "name": "Samsung", "websiteUrl": "https://samsung.com" }
```

---

#### `POST /api/brands`
Creates a new brand.

- **Handler:** `BrandController.create(payload: BrandCreateRequest)`
- **Response:** `201 Created` — `BrandResponse`

**Request body (`BrandCreateRequest`):**
| Field | Type | Required |
|---|---|---|
| `name` | `String` | required |
| `websiteUrl` | `String?` | optional |

```json
{ "name": "Sony", "websiteUrl": "https://sony.com" }
```

**Response body:**
```json
{ "id": 3, "name": "Sony", "websiteUrl": "https://sony.com" }
```

---

#### `PATCH /api/brands/{id}`
Partially updates an existing brand. Only provided fields are changed.

- **Handler:** `BrandController.update(id: Long, request: BrandUpdateRequest)`
- **Path variable:** `id` — `Long`
- **Response:** `200 OK` — `BrandResponse`
- **Errors:** `404 Not Found`

**Request body (`BrandUpdateRequest`):**
| Field | Type | Required |
|---|---|---|
| `name` | `String?` | optional |
| `websiteUrl` | `String?` | optional |

```json
{ "websiteUrl": "https://new-sony.com" }
```

---

#### `DELETE /api/brands/{id}`
Hard-deletes a brand.

- **Handler:** `BrandController.delete(id: Long)`
- **Path variable:** `id` — `Long`
- **Response:** `204 No Content`
- **Errors:** `404 Not Found`

---

### Categories — `CategoryController` (`/categories`)

#### `GET /api/categories`
Returns all categories (flat list).

- **Handler:** `CategoryController.getAll()`
- **Response:** `200 OK` — `List<CategoryResponse>`

**`CategoryResponse` fields:**
| Field | Type |
|---|---|
| `id` | `Long` |
| `parentId` | `Long?` |
| `name` | `String` |
| `slug` | `String` |

```json
[
  { "id": 1, "parentId": null, "name": "Electronics", "slug": "electronics" },
  { "id": 2, "parentId": 1,    "name": "Smartphones",  "slug": "smartphones" }
]
```

---

#### `GET /api/categories/{id}`
Returns a single category.

- **Handler:** `CategoryController.getById(id: Long)`
- **Response:** `200 OK` — `CategoryResponse`
- **Errors:** `404 Not Found`

---

#### `POST /api/categories`
Creates a new category.

- **Handler:** `CategoryController.create(payload: CategoryCreateRequest)`
- **Response:** `201 Created` — `CategoryResponse`
- **Errors:** `404 Not Found` if `parentId` is provided but does not exist

**Request body (`CategoryCreateRequest`):**
| Field | Type | Required |
|---|---|---|
| `parentId` | `Long?` | optional |
| `name` | `String` | required |
| `slug` | `String` | required |

```json
{ "parentId": 1, "name": "Tablets", "slug": "tablets" }
```

---

#### `PATCH /api/categories/{id}`
Partially updates a category. Only provided fields are changed. `parentId` in the update body replaces the existing parent; it cannot be cleared to `null` through this endpoint (providing `null` leaves the existing parent unchanged).

- **Handler:** `CategoryController.update(id: Long, request: CategoryUpdateRequest)`
- **Response:** `200 OK` — `CategoryResponse`
- **Errors:** `404 Not Found`

**Request body (`CategoryUpdateRequest`):**
| Field | Type | Required |
|---|---|---|
| `parentId` | `Long?` | optional |
| `name` | `String?` | optional |
| `slug` | `String?` | optional |

---

#### `DELETE /api/categories/{id}`
Hard-deletes a category.

- **Handler:** `CategoryController.delete(id: Long)`
- **Response:** `204 No Content`
- **Errors:** `404 Not Found`

---

### Product Types — `ProductTypeController` (`/product-types`)

#### `GET /api/product-types`
Returns all product types.

- **Handler:** `ProductTypeController.getAll()`
- **Response:** `200 OK` — `List<ProductTypeResponse>`

**`ProductTypeResponse` fields:**
| Field | Type |
|---|---|
| `id` | `Long` |
| `categoryId` | `Long` |
| `name` | `String` |

---

#### `GET /api/product-types/{id}`
Returns a single product type.

- **Handler:** `ProductTypeController.getById(id: Long)`
- **Response:** `200 OK` — `ProductTypeResponse`
- **Errors:** `404 Not Found`

---

#### `GET /api/product-types/{id}/attributes`
Returns the attribute schema (list of `CategoryParameter`) for a given product type. The schema is derived from all `CategoryParameter` records belonging to the product type's category.

- **Handler:** `ProductTypeController.getAttributeSchema(id: Long)`
- **Response:** `200 OK` — `List<CategoryParameterResponse>`
- **Errors:** `404 Not Found` if product type does not exist

**`CategoryParameterResponse` fields:**
| Field | Type |
|---|---|
| `id` | `Long` |
| `name` | `String` |
| `code` | `String?` |
| `order` | `Int` |
| `categoryId` | `Long` |
| `parameterType` | `ParameterType` (enum string) |
| `intValue` | `Int?` |
| `doubleValue2` | `Int?` |
| `doubleValue3` | `Int?` |
| `booleanValue` | `Boolean?` |
| `stringValue` | `String?` |
| `parameterGroupId` | `Long?` |

---

#### `POST /api/product-types`
Creates a new product type linked to a category.

- **Handler:** `ProductTypeController.create(payload: ProductTypeCreateRequest)`
- **Response:** `201 Created` — `ProductTypeResponse`
- **Errors:** `404 Not Found` if `categoryId` does not exist

**Request body (`ProductTypeCreateRequest`):**
| Field | Type | Required |
|---|---|---|
| `categoryId` | `Long` | required |
| `name` | `String` | required |

```json
{ "categoryId": 2, "name": "Android Phone" }
```

---

#### `PATCH /api/product-types/{id}`
Partially updates a product type.

- **Handler:** `ProductTypeController.update(id: Long, request: ProductTypeUpdateRequest)`
- **Response:** `200 OK` — `ProductTypeResponse`
- **Errors:** `404 Not Found`

**Request body (`ProductTypeUpdateRequest`):**
| Field | Type | Required |
|---|---|---|
| `categoryId` | `Long?` | optional |
| `name` | `String?` | optional |

---

#### `DELETE /api/product-types/{id}`
Hard-deletes a product type.

- **Handler:** `ProductTypeController.delete(id: Long)`
- **Response:** `204 No Content`
- **Errors:** `404 Not Found`

---

### Products — `ProductController` (`/products`)

#### `GET /api/products`
Returns a paginated list of products with optional filters.

- **Handler:** `ProductController.getAll(categoryId, status, pageable)`
- **Query parameters:**
  | Parameter | Type | Default | Description |
  |---|---|---|---|
  | `categoryId` | `Long?` | none | Filter by category ID |
  | `status` | `ProductStatus?` | none | Filter by status (`DRAFT`, `ACTIVE`, `ARCHIVED`) |
  | `page` | `Int` | `0` | Page index (Spring `Pageable`) |
  | `size` | `Int` | `20` | Page size (Spring `Pageable`) |
  | `sort` | `String` | none | Sort expression, e.g. `name,asc` |
- **Response:** `200 OK` — `Page<ProductResponse>` (Spring Data `Page` wrapper)

**`ProductResponse` fields:**
| Field | Type |
|---|---|
| `id` | `Long` |
| `sku` | `String` |
| `name` | `String` |
| `brandId` | `Long` |
| `categoryId` | `Long` |
| `productTypeId` | `Long` |
| `description` | `String?` |
| `status` | `ProductStatus` |
| `createdAt` | `Instant` (ISO-8601) |
| `updatedAt` | `Instant` (ISO-8601) |

**Example request:**
```
GET /api/products?categoryId=2&status=ACTIVE&page=0&size=10&sort=name,asc
```

**Example response (abbreviated):**
```json
{
  "content": [
    {
      "id": 42,
      "sku": "SAM-S24-BLK",
      "name": "Samsung Galaxy S24",
      "brandId": 1,
      "categoryId": 2,
      "productTypeId": 5,
      "description": "Flagship Android smartphone",
      "status": "ACTIVE",
      "createdAt": "2024-01-15T10:00:00Z",
      "updatedAt": "2024-03-01T12:30:00Z"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

---

#### `GET /api/products/{id}`
Returns a single product by ID.

- **Handler:** `ProductController.getById(id: Long)`
- **Response:** `200 OK` — `ProductResponse`
- **Errors:** `404 Not Found`

---

#### `POST /api/products`
Creates a new product. The initial status is always `DRAFT`.

- **Handler:** `ProductController.create(payload: ProductCreateRequest)`
- **Response:** `201 Created` — `ProductResponse`
- **Errors:** `404 Not Found` if `brandId`, `categoryId`, or `productTypeId` does not exist

**Request body (`ProductCreateRequest`):**
| Field | Type | Required |
|---|---|---|
| `sku` | `String` | required — must be unique |
| `name` | `String` | required |
| `brandId` | `Long` | required |
| `categoryId` | `Long` | required |
| `productTypeId` | `Long` | required |
| `description` | `String?` | optional |

```json
{
  "sku": "SAM-S24-BLK",
  "name": "Samsung Galaxy S24",
  "brandId": 1,
  "categoryId": 2,
  "productTypeId": 5,
  "description": "Flagship Android smartphone"
}
```

---

#### `PATCH /api/products/{id}`
Partially updates a product. All fields are optional; only provided fields are changed. `sku` is not updatable through this endpoint.

- **Handler:** `ProductController.update(id: Long, request: ProductUpdateRequest)`
- **Response:** `200 OK` — `ProductResponse`
- **Errors:** `404 Not Found` if product or any referenced FK does not exist

**Request body (`ProductUpdateRequest`):**
| Field | Type | Required |
|---|---|---|
| `name` | `String?` | optional |
| `brandId` | `Long?` | optional |
| `categoryId` | `Long?` | optional |
| `productTypeId` | `Long?` | optional |
| `description` | `String?` | optional |
| `status` | `ProductStatus?` | optional (`DRAFT`, `ACTIVE`, `ARCHIVED`) |

```json
{ "status": "ACTIVE", "description": "Updated description" }
```

---

#### `DELETE /api/products/{id}`
Soft-archives a product by setting its status to `ARCHIVED`. The product record is **not** removed from the database.

- **Handler:** `ProductController.archive(id: Long)`
- **Response:** `204 No Content`
- **Errors:** `404 Not Found`

---

#### `POST /api/products/batch`
Fetches multiple products in a single request. Products are returned in the same order as the requested IDs.

- **Handler:** `ProductController.batchGet(request: ProductBatchRequest)`
- **Response:** `200 OK` — `ProductBatchResponse`
- **Errors:** `400 Bad Request` if `productIds` is empty or exceeds 100 items

**Validation constraints:**
- `productIds`: `@NotEmpty` — must not be empty
- `productIds`: `@Size(max=100)` — maximum 100 IDs per request

**Request body (`ProductBatchRequest`):**
| Field | Type | Constraints |
|---|---|---|
| `productIds` | `List<Long>` | required, 1–100 items |

```json
{ "productIds": [1001, 1002, 1003] }
```

**Response body (`ProductBatchResponse`):**
| Field | Type | Description |
|---|---|---|
| `products` | `List<ProductResponse>` | Found products, ordered by input ID order |
| `notFoundIds` | `List<Long>` | IDs from the request that do not exist |

```json
{
  "products": [
    {
      "id": 1001, "sku": "CPU-001", "name": "Intel Core i9-14900K",
      "brandId": 1, "categoryId": 101, "productTypeId": 201,
      "description": null, "status": "ACTIVE",
      "createdAt": "2024-01-01T00:00:00Z", "updatedAt": "2024-01-01T00:00:00Z"
    }
  ],
  "notFoundIds": [1003]
}
```

**Implementation notes:**
- Uses `ProductRepository.findAllWithRelationsByIds()` — single JPQL query with `JOIN FETCH` for brand, category, and product type. No N+1.

---

#### `GET /api/products/{id}/details`
Returns full product details including embedded brand, category, product type, and all attribute values with parameter codes and names. Designed for API aggregation and compatibility checks that need complete attribute data.

- **Handler:** `ProductController.getDetails(id: Long)`
- **Response:** `200 OK` — `ProductDetailsResponse`
- **Errors:** `404 Not Found`

**Response body (`ProductDetailsResponse`):**
| Field | Type | Description |
|---|---|---|
| `id` | `Long` | |
| `sku` | `String` | |
| `name` | `String` | |
| `description` | `String?` | |
| `status` | `ProductStatus` | |
| `brand` | `BrandInfo` | Embedded brand details |
| `category` | `CategoryInfo` | Embedded category details |
| `productType` | `ProductTypeInfo` | Embedded product type details |
| `attributes` | `List<AttributeInfo>` | All product attribute values with parameter metadata |
| `createdAt` | `Instant` | |
| `updatedAt` | `Instant` | |

**`BrandInfo`:** `id`, `name`, `websiteUrl?`

**`CategoryInfo`:** `id`, `name`, `slug`

**`ProductTypeInfo`:** `id`, `name`

**`AttributeInfo`:** `code?` (e.g. `"socket_type"`), `name` (e.g. `"Socket Type"`), `value` (e.g. `"LGA1700"`)

```json
{
  "id": 1001,
  "sku": "CPU-001",
  "name": "Intel Core i9-14900K",
  "description": "High-performance desktop CPU",
  "status": "ACTIVE",
  "brand": { "id": 1, "name": "Intel", "websiteUrl": "https://intel.com" },
  "category": { "id": 101, "name": "Processors", "slug": "processors" },
  "productType": { "id": 201, "name": "Desktop CPU" },
  "attributes": [
    { "code": "socket_type", "name": "Socket Type", "value": "LGA1700" },
    { "code": "tdp_watt",    "name": "TDP (W)",     "value": "125" }
  ],
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

**Implementation notes:**
- Query 1: `ProductRepository.findByIdWithRelations()` — `JOIN FETCH` for brand, category, product type.
- Query 2: `ProductAttributeRepository.findByProductIdWithParameter()` — `JOIN FETCH` for `categoryParameter`. Total: 2 queries, no N+1.

---

### Product Attributes — `ProductAttributeController` (`/products/{productId}/attributes`)

#### `GET /api/products/{productId}/attributes`
Returns all attribute values for a product.

- **Handler:** `ProductAttributeController.getByProduct(productId: Long)`
- **Path variable:** `productId` — `Long`
- **Response:** `200 OK` — `List<ProductAttributeResponse>`

**`ProductAttributeResponse` fields:**
| Field | Type |
|---|---|
| `id` | `Long` |
| `productId` | `Long` |
| `categoryParameterId` | `Long` |
| `value` | `String` |

```json
[
  { "id": 1, "productId": 42, "categoryParameterId": 7,  "value": "256" },
  { "id": 2, "productId": 42, "categoryParameterId": 8,  "value": "Black" }
]
```

---

#### `PUT /api/products/{productId}/attributes`
Atomically replaces **all** attribute values for a product. All existing attributes for the product are deleted, then the provided list is saved. Returns `200 OK` with the full list of saved attributes.

- **Handler:** `ProductAttributeController.setAttributes(productId: Long, payload: ProductAttributeSetRequest)`
- **Path variable:** `productId` — `Long`
- **Response:** `200 OK` — `List<ProductAttributeResponse>`
- **Errors:** `404 Not Found` if product does not exist; `404 Not Found` if any `categoryParameterId` does not exist

**Request body (`ProductAttributeSetRequest`):**
| Field | Type | Required |
|---|---|---|
| `attributes` | `List<ProductAttributeItemRequest>` | required |

**`ProductAttributeItemRequest` fields:**
| Field | Type | Required |
|---|---|---|
| `categoryParameterId` | `Long` | required |
| `value` | `String` | required |

```json
{
  "attributes": [
    { "categoryParameterId": 7, "value": "256" },
    { "categoryParameterId": 8, "value": "Black" }
  ]
}
```

**Response body:**
```json
[
  { "id": 1, "productId": 42, "categoryParameterId": 7,  "value": "256" },
  { "id": 2, "productId": 42, "categoryParameterId": 8,  "value": "Black" }
]
```

---

### Category Parameters — `CategoryParameterController` (`/category-parameters`)

#### `GET /api/category-parameters`
Returns all category parameters for a given category. `categoryId` is a **required** query parameter.

- **Handler:** `CategoryParameterController.getByCategory(categoryId: Long)`
- **Query parameters:**
  | Parameter | Type | Required |
  |---|---|---|
  | `categoryId` | `Long` | required |
- **Response:** `200 OK` — `List<CategoryParameterResponse>`

---

#### `GET /api/category-parameters/{id}`
Returns a single category parameter by ID.

- **Handler:** `CategoryParameterController.getById(id: Long)`
- **Response:** `200 OK` — `CategoryParameterResponse`
- **Errors:** `404 Not Found`

---

#### `POST /api/category-parameters`
Creates a new category parameter.

- **Handler:** `CategoryParameterController.create(payload: CategoryParameterCreateRequest)`
- **Response:** `201 Created` — `CategoryParameterResponse`
- **Errors:** `404 Not Found` if `categoryId` or `parameterGroupId` does not exist

**Request body (`CategoryParameterCreateRequest`):**
| Field | Type | Required |
|---|---|---|
| `name` | `String` | required |
| `code` | `String?` | optional — machine-readable key (e.g. `"storage_gb"`) |
| `order` | `Int` | optional, default `0` |
| `categoryId` | `Long` | required |
| `parameterType` | `ParameterType` | required |
| `intValue` | `Int?` | optional |
| `doubleValue2` | `Int?` | optional |
| `doubleValue3` | `Int?` | optional |
| `booleanValue` | `Boolean?` | optional |
| `stringValue` | `String?` | optional |
| `parameterGroupId` | `Long?` | optional (required for `LIST` type in practice) |

```json
{
  "name": "Storage",
  "order": 1,
  "categoryId": 2,
  "parameterType": "INT",
  "intValue": 256
}
```

---

#### `PATCH /api/category-parameters/{id}`
Partially updates a category parameter. `categoryId` cannot be changed via update.

- **Handler:** `CategoryParameterController.update(id: Long, request: CategoryParameterUpdateRequest)`
- **Response:** `200 OK` — `CategoryParameterResponse`
- **Errors:** `404 Not Found`

**Request body (`CategoryParameterUpdateRequest`):**
| Field | Type | Required |
|---|---|---|
| `name` | `String?` | optional |
| `order` | `Int?` | optional |
| `parameterType` | `ParameterType?` | optional |
| `intValue` | `Int?` | optional |
| `doubleValue2` | `Int?` | optional |
| `doubleValue3` | `Int?` | optional |
| `booleanValue` | `Boolean?` | optional |
| `stringValue` | `String?` | optional |
| `parameterGroupId` | `Long?` | optional |

---

#### `DELETE /api/category-parameters/{id}`
Hard-deletes a category parameter.

- **Handler:** `CategoryParameterController.delete(id: Long)`
- **Response:** `204 No Content`
- **Errors:** `404 Not Found`

---

### Parameter Groups — `ParameterGroupController` (`/parameter-groups`)

#### `GET /api/parameter-groups`
Returns all parameter groups.

- **Handler:** `ParameterGroupController.getAll()`
- **Response:** `200 OK` — `List<ParameterGroupResponse>`

**`ParameterGroupResponse` fields:** `id: Long`, `name: String`

---

#### `GET /api/parameter-groups/{id}`
Returns a single parameter group.

- **Handler:** `ParameterGroupController.getById(id: Long)`
- **Response:** `200 OK` — `ParameterGroupResponse`
- **Errors:** `404 Not Found`

---

#### `GET /api/parameter-groups/{id}/items`
Returns all items (enum values) of a parameter group.

- **Handler:** `ParameterGroupController.getItems(id: Long)`
- **Response:** `200 OK` — `List<ParameterGroupItemResponse>`
- **Errors:** `404 Not Found` if the group does not exist

**`ParameterGroupItemResponse` fields:**
| Field | Type |
|---|---|
| `id` | `Long` |
| `name` | `String` |
| `parameterGroupId` | `Long` |

---

#### `POST /api/parameter-groups`
Creates a new parameter group.

- **Handler:** `ParameterGroupController.create(payload: ParameterGroupCreateRequest)`
- **Response:** `201 Created` — `ParameterGroupResponse`

**Request body (`ParameterGroupCreateRequest`):** `{ "name": "Color" }`

---

#### `PATCH /api/parameter-groups/{id}`
Renames a parameter group. `name` is **required** (not nullable in `ParameterGroupUpdateRequest`).

- **Handler:** `ParameterGroupController.update(id: Long, request: ParameterGroupUpdateRequest)`
- **Response:** `200 OK` — `ParameterGroupResponse`
- **Errors:** `404 Not Found`

**Request body (`ParameterGroupUpdateRequest`):** `{ "name": "Colour" }`

---

#### `DELETE /api/parameter-groups/{id}`
Hard-deletes a parameter group.

- **Handler:** `ParameterGroupController.delete(id: Long)`
- **Response:** `204 No Content`
- **Errors:** `404 Not Found`

---

#### `POST /api/parameter-groups/{id}/items`
Adds a new item to a parameter group.

- **Handler:** `ParameterGroupController.addItem(id: Long, payload: ParameterGroupItemAddRequest)`
- **Path variable:** `id` — group ID
- **Response:** `201 Created` — `ParameterGroupItemResponse`
- **Errors:** `404 Not Found` if group does not exist

**Request body (`ParameterGroupItemAddRequest`):** `{ "name": "Red" }`

---

#### `DELETE /api/parameter-groups/items/{itemId}`
Deletes a single item from a parameter group.

- **Handler:** `ParameterGroupController.deleteItem(itemId: Long)`
- **Path variable:** `itemId` — `Long`
- **Response:** `204 No Content`
- **Errors:** `404 Not Found`

---

### Components — `ComponentController` (`/components`)

#### `GET /api/components/groups`
Returns all component groups.

- **Handler:** `ComponentController.getAllGroups()`
- **Response:** `200 OK` — `List<ComponentGroupResponse>`

**`ComponentGroupResponse` fields:** `id: Long`, `name: String`

---

#### `GET /api/components`
Returns all components for a given group. `groupId` is a **required** query parameter.

- **Handler:** `ComponentController.getByGroup(groupId: Long)`
- **Query parameters:**
  | Parameter | Type | Required |
  |---|---|---|
  | `groupId` | `Long` | required |
- **Response:** `200 OK` — `List<ComponentResponse>`
- **Errors:** `404 Not Found` if group does not exist

**`ComponentResponse` fields:** `id: Long`, `name: String`, `componentGroupId: Long`

---

#### `GET /api/components/{id}`
Returns a single component.

- **Handler:** `ComponentController.getById(id: Long)`
- **Response:** `200 OK` — `ComponentResponse`
- **Errors:** `404 Not Found`

---

#### `POST /api/components/groups`
Creates a new component group.

- **Handler:** `ComponentController.createGroup(payload: ComponentGroupCreateRequest)`
- **Response:** `201 Created` — `ComponentGroupResponse`

**Request body (`ComponentGroupCreateRequest`):** `{ "name": "Processor" }`

---

#### `POST /api/components`
Creates a new component inside a group.

- **Handler:** `ComponentController.create(payload: ComponentCreateRequest)`
- **Response:** `201 Created` — `ComponentResponse`
- **Errors:** `404 Not Found` if `componentGroupId` does not exist

**Request body (`ComponentCreateRequest`):**
| Field | Type | Required |
|---|---|---|
| `name` | `String` | required |
| `componentGroupId` | `Long` | required |

```json
{ "name": "Apple A17 Pro", "componentGroupId": 1 }
```

---

#### `PATCH /api/components/{id}`
Updates a component name. `name` is **required** (not nullable in `ComponentUpdateRequest`).

- **Handler:** `ComponentController.update(id: Long, request: ComponentUpdateRequest)`
- **Response:** `200 OK` — `ComponentResponse`
- **Errors:** `404 Not Found`

**Request body (`ComponentUpdateRequest`):** `{ "name": "Apple A18 Pro" }`

---

#### `DELETE /api/components/{id}`
Hard-deletes a component.

- **Handler:** `ComponentController.delete(id: Long)`
- **Response:** `204 No Content`
- **Errors:** `404 Not Found`

---

## 5. Business Logic

### `ProductService`

| Method | Behavior |
|---|---|
| `findAll(categoryId, status, pageable)` | Builds a JPA `Specification` dynamically from the nullable filters and delegates to `JpaSpecificationExecutor`. Returns a `Page<Product>`. Max page size is capped at 100 via `PageableHandlerMethodArgumentResolverCustomizer`. |
| `findById(id)` | Throws `ResponseStatusException(404)` if not found. Uses standard `findById` (no JOIN FETCH; relies on `@BatchSize` to avoid N+1 in list contexts). |
| `batchGet(ids)` | Loads all requested products in one `JOIN FETCH` query (`findAllWithRelationsByIds`). Returns `Pair<List<Product>, List<Long>>` — the found products (ordered by input), and the IDs that were not found. |
| `findDetails(id)` | Executes two queries: (1) `findByIdWithRelations` — `JOIN FETCH` for brand, category, product type; (2) `findByProductIdWithParameter` — `JOIN FETCH` for category parameters. Returns `ProductWithAttributesDto`. |
| `create(dto)` | Validates that `brandId`, `categoryId`, and `productTypeId` all exist (each throws 404 if not). Creates the product with `status = DRAFT`. `sku` uniqueness is checked explicitly (409) before insert. |
| `update(id, dto)` | All fields are optional. If a FK field is `null` in the DTO, the existing FK is retained. If a FK field is provided, the referenced entity is validated. The `sku` field is immutable: `update` always copies `existing.sku`. |
| `archive(id)` | Sets `status = ARCHIVED` and updates `updatedAt`. Does **not** delete the row. This is the only action performed by `DELETE /api/products/{id}`. |

**N+1 mitigation strategy:**
- `GET /products` list: `@BatchSize(size=25)` on `Brand`, `Category`, `ProductType`, `Product` entities causes Hibernate to batch-load lazy associations in groups, avoiding per-row SELECT.
- `POST /products/batch`: explicit `JOIN FETCH` in a single JPQL query.
- `GET /products/{id}/details`: 2 explicit queries with `JOIN FETCH`.

### `CategoryParameterService`

- `create`: validates `categoryId` and optionally `parameterGroupId`. The `categoryId` cannot be changed on update.
- `update`: the update DTO has no `categoryId` field, so the category FK is always preserved from the existing record.

### `ProductAttributeService`

- `setProductAttributes(productId, dto)`: runs in a single `@Transactional` block. It first issues a bulk `DELETE` (`deleteByProductId`) with `@Modifying(clearAutomatically = true)`, then validates and saves all new attributes. If any `categoryParameterId` is invalid the entire transaction rolls back.
- `deleteByProductId(productId)`: standalone transactional method for use by other services or future orchestration.

### `ProductTypeService`

- `findAttributeSchema(productTypeId)`: resolves the category of the product type, then returns all `CategoryParameter` records for that category. This is the primary mechanism for a client to know which attributes are expected before calling `PUT /products/{id}/attributes`.

### Transaction behavior

All write operations (`create`, `update`, `delete`, `archive`, `setProductAttributes`) are annotated with `@Transactional`. The transaction manager is `pgTransactionManager` (JPA, Hibernate-backed), configured with `isRollbackOnCommitFailure = true`. Read methods are not transactional.

---

## 6. Inter-Service Communication

No inter-service communication is currently implemented. `product-service` does not make any outbound HTTP calls to other microservices.

---

## 7. Kafka and Asynchronous Events

Kafka is not implemented. The `build.gradle.kts` contains no Kafka dependency, and no event producers or consumers exist in the source code.

---

## 8. Error Handling

`GlobalExceptionHandler` (`com.rest.GlobalExceptionHandler`) is a `@RestControllerAdvice` that maps exceptions to structured JSON error responses.

### Error response format

**Standard error (4xx / 5xx):**
```json
{ "status": 404, "error": "Product 999 not found" }
```

**Validation error (400):**
```json
{
  "status": 400,
  "error": "Validation failed",
  "details": ["sku: must not be blank", "brandId: must be greater than 0"]
}
```

### HTTP status mapping

| Scenario | Status | Handler |
|---|---|---|
| Entity not found | `404 Not Found` | `ResponseStatusException` → `GlobalExceptionHandler` |
| SKU already exists | `409 Conflict` | `ResponseStatusException` → `GlobalExceptionHandler` |
| Validation failure (`@Valid`) | `400 Bad Request` | `MethodArgumentNotValidException` → `GlobalExceptionHandler` |
| Oversized batch request | `400 Bad Request` | `@Size(max=100)` → `MethodArgumentNotValidException` |
| Successful read | `200 OK` | — |
| Successful create | `201 Created` | — |
| Successful delete / archive | `204 No Content` | — |
| Unhandled exception | `500 Internal Server Error` | `Exception` → `GlobalExceptionHandler` |

### Request validation

`@Valid` is applied to `@RequestBody` on all write endpoints:

| Endpoint | Validated DTO | Constraints |
|---|---|---|
| `POST /products` | `ProductCreateRequest` | `sku`: `@NotBlank`; `name`: `@NotBlank`; `brandId`, `categoryId`, `productTypeId`: `@Positive` |
| `PATCH /products/{id}` | `ProductUpdateRequest` | `name`: `@Size(min=1)` if provided; `brandId`, `categoryId`, `productTypeId`: `@Positive` if provided |
| `POST /products/batch` | `ProductBatchRequest` | `productIds`: `@NotEmpty`, `@Size(max=100)` |

---

## 9. Configuration and Deployment

### Application settings

| Property (application.yml) | Env variable | Default | Description |
|---|---|---|---|
| `server.port` | `PRODUCT_SERVER_PORT` | `8080` | HTTP port |
| `server.servlet.context-path` | `PRODUCT_API_PREFIX` | `/api` | URL prefix for all endpoints |
| `app.datasource.postgres.host` | `PRODUCT_POSTGRES_HOST` | `localhost` | PostgreSQL host |
| `app.datasource.postgres.port` | `PRODUCT_POSTGRES_PORT` | `5432` | PostgreSQL port |
| `app.datasource.postgres.db` | `PRODUCT_POSTGRES_DB` | _(required)_ | Database name |
| `app.datasource.postgres.user` | `PRODUCT_POSTGRES_USER` | _(required)_ | Database username |
| `app.datasource.postgres.pass` | `PRODUCT_POSTGRES_PASSWORD` | _(required)_ | Database password |
| `app.datasource.postgres.ssl` | _(hardcoded)_ | `disable` | SSL mode |
| `app.datasource.postgres.pool-max` | `PRODUCT_HIKARI_POOL_MAX` | `25` | HikariCP max pool size |
| `app.datasource.postgres.pool-min` | `PRODUCT_HIKARI_POOL_MIN` | `2` | HikariCP min idle connections |
| `app.datasource.postgres.pool-timeout-millis` | `PRODUCT_HIKARI_POOL_TIMEOUT_MILLIS` | `30000` | HikariCP connection timeout (ms) |
| `app.datasource.postgres.show-sql` | `PRODUCT_POSTGRES_SHOW_SQL` | `true` | Log SQL to stdout |
| `app.datasource.postgres.ddl-auto` | `PRODUCT_POSTGRES_DDL_AUTO` | `update` | Hibernate DDL strategy |
| `app.seed.enable` | `PRODUCT_SEED_ENABLE` | `false` | Run data seed on startup |
| `app.seed.mode` | `PRODUCT_SEED_MODE` | `init` | Seed data directory (`init` or `prod`) |
| `app.seed.stop-count` | `PRODUCT_SEED_STOP_COUNT` | `1` | Skip seed if table row count exceeds this value |

### OpenAPI / Swagger UI

| Path | Description |
|---|---|
| `GET /api/v3/api-docs` | OpenAPI 3 JSON spec |
| `GET /api/swagger-ui.html` | Swagger UI |

### Docker

The multi-stage `Dockerfile` builds the service from source using `gradle:8.10-jdk21`, copies the resulting JAR into an `eclipse-temurin:21-jre-alpine` runtime image, and exposes port `8080`.

```bash
# Build image
docker build -t product-service .

# Run container (example)
docker run -p 8080:8080 \
  -e PRODUCT_POSTGRES_HOST=postgres \
  -e PRODUCT_POSTGRES_DB=product_db \
  -e PRODUCT_POSTGRES_USER=postgres \
  -e PRODUCT_POSTGRES_PASSWORD=postgres \
  product-service
```

The Gradle build task used is `:product-service:bootJar` (tests are skipped with `-x test`).

### Local development

```bash
# Start PostgreSQL (from project root)
docker compose up postgres-product -d

# Run the service
./gradlew :product-service:bootRun
```

---

## 10. Testing

No test classes exist. The directory `src/test/` is absent entirely from the project. The `spring-boot-starter-test` dependency is declared in `build.gradle.kts` but no test code has been written.

---

## 11. Known Limitations and Missing Functionality

### Business logic gaps

- **`sku` is immutable but there is no explicit error**: Attempting to change the SKU is silently ignored — the update method copies `existing.sku` unconditionally. No `400` or explicit error is returned.
- **Category delete does not cascade-guard**: Deleting a category that has associated products, product types, or category parameters will produce a database foreign key violation, surfacing as an unhandled `500`.
- **`parentId` on category update cannot be cleared**: Setting `parentId: null` in `CategoryUpdateRequest` will not remove an existing parent — the update logic falls back to `existing.parent`. There is no way to convert a child category to a root category via the API.
- **`ParameterGroupUpdateRequest.name` and `ComponentUpdateRequest.name` are non-nullable**: PATCH semantics for these endpoints effectively require a name value (full rename, not a partial update).
- **`GET /products` N+1 for large pages**: `@BatchSize(size=25)` reduces extra queries to `ceil(pageSize/25)` batches per association, but does not eliminate them entirely. For large pages a `JOIN FETCH` with count-query separation would be needed.

### Infrastructure gaps

- **No Kafka**: No domain events are published when products are created, updated, or archived. Downstream services have no way to react to catalog changes asynchronously.
- **No authentication/authorization**: Spring Security is present but configured to permit all requests without any authentication. There is no JWT validation or role-based access control.
- **No database migrations**: The service relies on `hibernate.hbm2ddl.auto = update`. There are no Flyway or Liquibase migration scripts.
- **Seed data is `init`-mode only**: The `static/data/prod/` directory contains only `category.json`. Running seed with `PRODUCT_SEED_MODE=prod` will fail for all seeders except `CategoryDataSeed`.
- **No integration tests**: No test classes exist. `spring-boot-starter-test` is declared but unused. Batch and details endpoints are tested only manually.
