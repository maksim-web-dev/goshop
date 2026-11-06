# GoShop

Мультимодульний e-commerce монорепозиторій на Kotlin / Spring Boot 3.

| Сервіс | Порт | Відповідальність |
|---|---|---|
| `product-service` | `8080` | каталог товарів, категорій, брендів, типів та характеристик |
| `compatibility-service` | `8082` | перевірка сумісності комплектуючих за правилами |
| `localization-service` | `8083` | переклади локалізованих полів товарів |
| `pricing-service` | `8084` | ціни, знижки, курси валют, історія цін |
| `recommendations-service` | `8085` | підбір схожих, альтернативних і доповнюючих товарів |
| `search-service` | `8086` | повнотекстовий пошук і фільтрація (PostgreSQL FTS) |
| `store-service` | `8087` | управління магазинами, API-ключами та webhook-ами |

---

## Product Service

### Endpoints

| Метод | Шлях | Опис |
|---|---|---|
| `GET` | `/api/brands` | список усіх брендів |
| `GET` | `/api/brands/{id}` | бренд за ID |
| `POST` | `/api/brands` | створити бренд |
| `PATCH` | `/api/brands/{id}` | оновити бренд |
| `DELETE` | `/api/brands/{id}` | видалити бренд |
| `GET` | `/api/categories` | список усіх категорій |
| `GET` | `/api/categories/{id}` | категорія за ID |
| `POST` | `/api/categories` | створити категорію |
| `PATCH` | `/api/categories/{id}` | оновити категорію |
| `DELETE` | `/api/categories/{id}` | видалити категорію |
| `GET` | `/api/product-types` | список усіх типів товарів |
| `GET` | `/api/product-types/{id}` | тип за ID |
| `GET` | `/api/product-types/{id}/attributes` | схема характеристик типу |
| `POST` | `/api/product-types` | створити тип |
| `PATCH` | `/api/product-types/{id}` | оновити тип |
| `DELETE` | `/api/product-types/{id}` | видалити тип |
| `GET` | `/api/products` | список товарів (пагінація; фільтри: `categoryId`, `status`) |
| `GET` | `/api/products/{id}` | товар за ID |
| `POST` | `/api/products` | створити товар |
| `PATCH` | `/api/products/{id}` | оновити товар |
| `DELETE` | `/api/products/{id}` | архівувати товар (→ `ARCHIVED`) |
| `GET` | `/api/products/{productId}/attributes` | всі характеристики товару |
| `PUT` | `/api/products/{productId}/attributes` | замінити всі характеристики товару |
| `GET` | `/api/category-parameters` | параметри за категорією (`?categoryId=`) |
| `GET` | `/api/category-parameters/{id}` | параметр за ID |
| `POST` | `/api/category-parameters` | створити параметр |
| `PATCH` | `/api/category-parameters/{id}` | оновити параметр |
| `DELETE` | `/api/category-parameters/{id}` | видалити параметр |
| `GET` | `/api/parameter-groups` | список усіх груп параметрів |
| `GET` | `/api/parameter-groups/{id}` | група за ID |
| `GET` | `/api/parameter-groups/{id}/items` | елементи групи |
| `POST` | `/api/parameter-groups` | створити групу |
| `PATCH` | `/api/parameter-groups/{id}` | оновити групу |
| `DELETE` | `/api/parameter-groups/{id}` | видалити групу |
| `POST` | `/api/parameter-groups/{id}/items` | додати елемент до групи |
| `DELETE` | `/api/parameter-groups/items/{itemId}` | видалити елемент групи |
| `GET` | `/api/components/groups` | список груп компонентів |
| `GET` | `/api/components` | компоненти за групою (`?groupId=`) |
| `GET` | `/api/components/{id}` | компонент за ID |
| `POST` | `/api/components/groups` | створити групу компонентів |
| `POST` | `/api/components` | створити компонент |
| `PATCH` | `/api/components/{id}` | оновити компонент |
| `DELETE` | `/api/components/{id}` | видалити компонент |

### Entities

**Brand** — `id` `Long` · `name` `String` · `websiteUrl` `String?`

**Category** — `id` `Long` · `parent` `Category?` · `name` `String` · `slug` `String`

**ProductType** — `id` `Long` · `category` `Category` · `name` `String`

**Product** — `id` `Long` · `sku` `String` · `name` `String` · `brand` `Brand` · `category` `Category` · `productType` `ProductType` · `description` `String?` · `status` `ProductStatus` (`DRAFT`/`ACTIVE`/`ARCHIVED`) · `createdAt` `Instant` · `updatedAt` `Instant`

**ParameterGroup** — `id` `Long` · `name` `String`

**ParameterGroupItem** — `id` `Long` · `parameterGroup` `ParameterGroup` · `name` `String`

**CategoryParameter** — `id` `Long` · `category` `Category` · `name` `String` · `order` `Int` · `parameterType` `ParameterType` (`INT`/`DOUBLE_2`/`DOUBLE_3`/`STRING`/`BOOLEAN`/`LIST`) · `intValue` `Int?` · `doubleValue2` `Int?` · `doubleValue3` `Int?` · `booleanValue` `Boolean?` · `stringValue` `String?` · `parameterGroup` `ParameterGroup?`

**ProductAttribute** — `id` `Long` · `product` `Product` · `categoryParameter` `CategoryParameter` · `value` `String`

**ComponentGroup** — `id` `Long` · `name` `String`

**Component** — `id` `Long` · `componentGroup` `ComponentGroup` · `name` `String`

---

## Compatibility Service

### Endpoints

| Метод | Шлях | Опис |
|---|---|---|
| `POST` | `/api/compatibility/check` | перевірити набір комплектуючих |
| `POST` | `/api/compatibility/pairs/check` | перевірити пару товарів |
| `GET` | `/api/compatibility/products/{id}` | знайти сумісні товари для конкретного товару |
| `GET` | `/api/compatibility/rules` | список правил |
| `POST` | `/api/compatibility/rules` | створити правило |
| `PUT` | `/api/compatibility/rules/{id}` | оновити правило |
| `DELETE` | `/api/compatibility/rules/{id}` | вимкнути правило |

### Entities

**CompatibilityRule** — `id` `UUID` · `code` `String` (унікальний) · `sourceProductTypeId` `UUID` · `targetProductTypeId` `UUID` · `ruleType` `RuleType` (`SOCKET_MATCH`/`RAM_COMPATIBILITY`/`POWER_CAPACITY`/`GPU_CLEARANCE`/`COOLING_TDP`/`FORM_FACTOR`) · `parameters` `String?` (JSONB) · `severity` `Severity` (`ERROR`/`WARNING`/`INFO`) · `enabled` `Boolean` · `version` `Int`

**CompatibilityCheck** — `id` `UUID` · `productIds` `List<UUID>` · `compatible` `Boolean` · `checkedAt` `Instant`

**CompatibilityIssue** — `id` `UUID` · `check` `CompatibilityCheck` · `rule` `CompatibilityRule` · `severity` `Severity` · `messageCode` `String`

---

## Localization Service

### Endpoints

| Метод | Шлях | Опис |
|---|---|---|
| `GET` | `/api/languages` | список усіх мов |
| `GET` | `/api/translations` | список перекладів (фільтри: `entityType`, `entityId`, `languageCode`) |
| `POST` | `/api/translations` | створити переклад |
| `PUT` | `/api/translations/{id}` | оновити переклад |
| `DELETE` | `/api/translations/{id}` | видалити переклад |
| `POST` | `/api/translations/resolve` | отримати переклад з fallback |
| `POST` | `/api/translation-jobs` | запустити завдання перекладу |
| `GET` | `/api/translation-jobs/{id}` | статус завдання |

### Entities

**Language** — `code` `String` (PK, BCP 47) · `name` `String` · `enabled` `Boolean`

**Translation** — `id` `UUID` · `entityType` `EntityType` (`PRODUCT`/`BRAND`/`CATEGORY`/`PRODUCT_TYPE`) · `entityId` `UUID` · `fieldName` `String` · `language` `Language` · `translatedValue` `String` · `status` `TranslationStatus` (`DRAFT`/`APPROVED`/`AUTO_TRANSLATED`) · `updatedAt` `Instant` — unique(`entityType`, `entityId`, `fieldName`, `languageCode`)

**TranslationJob** — `id` `UUID` · `entityType` `EntityType` · `entityId` `UUID` · `targetLanguage` `Language` · `status` `TranslationJobStatus` (`PENDING`/`IN_PROGRESS`/`DONE`/`FAILED`) · `attempts` `Int` · `createdAt` `Instant` · `completedAt` `Instant?`

---

## Pricing Service

### Endpoints

| Метод | Шлях | Опис |
|---|---|---|
| `GET` | `/api/prices/products/{productId}` | актуальні ціни товару |
| `POST` | `/api/prices` | створити ціну |
| `PUT` | `/api/prices/{id}` | оновити ціну |
| `GET` | `/api/prices/{id}/history` | історія змін ціни |
| `POST` | `/api/prices/batch` | ціни для списку товарів |
| `POST` | `/api/prices/calculate` | фінальна ціна зі знижками |
| `GET` | `/api/discounts` | список знижок |
| `POST` | `/api/discounts` | створити знижку |
| `PUT` | `/api/discounts/{id}` | оновити знижку |
| `DELETE` | `/api/discounts/{id}` | вимкнути знижку |
| `GET` | `/api/exchange-rates` | останні курси валют |

### Entities

**Price** — `id` `UUID` · `productId` `UUID` · `storeId` `UUID?` (null = базова ціна) · `amount` `BigDecimal` · `currencyCode` `String` · `validFrom` `Instant` · `validTo` `Instant?` · `updatedAt` `Instant`

**Discount** — `id` `UUID` · `storeId` `UUID` · `productId` `UUID?` (null = весь асортимент) · `discountType` `DiscountType` (`PERCENTAGE`/`FIXED`) · `discountValue` `BigDecimal` · `startsAt` `Instant` · `endsAt` `Instant?` · `status` `DiscountStatus` (`ACTIVE`/`INACTIVE`/`EXPIRED`)

**ExchangeRate** — `id` `UUID` · `fromCurrency` `String` · `toCurrency` `String` · `rate` `BigDecimal` · `effectiveAt` `Instant`

**PriceHistory** — `id` `UUID` · `price` `Price` · `oldAmount` `BigDecimal` · `newAmount` `BigDecimal` · `changedAt` `Instant`

---

## Recommendations Service

### Endpoints

| Метод | Шлях | Опис |
|---|---|---|
| `GET` | `/api/recommendations/products/{id}` | загальні рекомендації для товару |
| `GET` | `/api/recommendations/products/{id}/similar` | схожі товари |
| `GET` | `/api/recommendations/products/{id}/alternatives` | альтернативи |
| `GET` | `/api/recommendations/products/{id}/accessories` | аксесуари |
| `POST` | `/api/recommendations/build` | рекомендації для збірки ПК |
| `GET` | `/api/recommendations/rules` | список правил |
| `POST` | `/api/recommendations/rules` | створити правило |
| `PUT` | `/api/recommendations/rules/{id}` | оновити правило |

### Entities

**RecommendationRule** — `id` `UUID` · `name` `String` · `strategy` `RecommendationStrategy` (`SIMILAR_ATTRIBUTES`/`SAME_CATEGORY`/`PRICE_RANGE`/`COMPATIBILITY_BASED`/`POPULAR_IN_CATEGORY`) · `parameters` `String?` (JSONB) · `priority` `Int` · `enabled` `Boolean`

**ProductRelation** — `id` `UUID` · `sourceProductId` `UUID` · `targetProductId` `UUID` · `relationType` `RelationType` (`SIMILAR`/`ALTERNATIVE`/`ACCESSORY`/`UPGRADE`) · `score` `Double` — unique(`sourceProductId`, `targetProductId`, `relationType`)

**RecommendationSnapshot** — `id` `UUID` · `productId` `UUID` · `strategy` `RecommendationStrategy` · `recommendedProductIds` `List<UUID>` · `generatedAt` `Instant`

---

## Search Service

### Endpoints

| Метод | Шлях | Опис |
|---|---|---|
| `GET` | `/api/search/products` | пошук товарів (`q`, `brand`, `categoryId`, `status`) |
| `POST` | `/api/search/products/query` | пошук зі складними фільтрами |
| `GET` | `/api/search/suggestions` | автодоповнення |
| `POST` | `/api/internal/search/reindex` | переіндексувати товар |
| `GET` | `/api/internal/search/index-jobs/{id}` | статус завдання індексації |

### Models

**SearchDocument** _(не JPA entity, PostgreSQL FTS)_ — `productId` `UUID` · `sku` `String` · `name` `String` · `localizedNames` `Map<String,String>` (JSONB) · `brand` `String` · `categoryId` `UUID` · `attributes` `Map<String,String>` (JSONB) · `searchableText` `String` (tsvector) · `status` `String` · `indexedAt` `Instant`

**SearchIndexJob** — `id` `UUID` · `productId` `UUID` · `operation` `IndexOperation` (`INDEX`/`UPDATE`/`DELETE`) · `status` `IndexJobStatus` (`PENDING`/`IN_PROGRESS`/`DONE`/`FAILED`) · `attempts` `Int` · `createdAt` `Instant`

---

## Store Service

### Endpoints

| Метод | Шлях | Опис |
|---|---|---|
| `GET` | `/api/stores` | список магазинів |
| `GET` | `/api/stores/{id}` | інформація про магазин |
| `POST` | `/api/stores` | зареєструвати магазин |
| `PATCH` | `/api/stores/{id}` | оновити магазин |
| `DELETE` | `/api/stores/{id}` | деактивувати магазин |
| `POST` | `/api/stores/{id}/api-keys` | видати API-ключ |
| `POST` | `/api/stores/{id}/api-keys/{keyId}/revoke` | відкликати API-ключ |
| `GET` | `/api/stores/{id}/settings` | налаштування магазину |
| `PUT` | `/api/stores/{id}/settings` | оновити налаштування |
| `POST` | `/api/stores/{id}/webhooks` | зареєструвати webhook |

### Entities

**Store** — `id` `UUID` · `name` `String` · `domain` `String` (унікальний) · `countryCode` `String` · `defaultLanguage` `String` · `defaultCurrency` `String` · `status` `StoreStatus` (`ACTIVE`/`INACTIVE`/`SUSPENDED`) · `createdAt` `Instant`

**StoreApiKey** — `id` `UUID` · `store` `Store` · `keyHash` `String` (bcrypt) · `keyPrefix` `String` · `status` `ApiKeyStatus` (`ACTIVE`/`REVOKED`/`EXPIRED`) · `createdAt` `Instant` · `expiresAt` `Instant?` · `lastUsedAt` `Instant?`

**StoreSettings** — `id` `UUID` · `store` `Store` (OneToOne) · `requestsPerSecond` `Int` · `burstLimit` `Int` · `allowedLanguages` `List<String>`

**StoreWebhook** — `id` `UUID` · `store` `Store` · `url` `String` · `eventTypes` `List<String>` · `secretRef` `String` · `enabled` `Boolean`
