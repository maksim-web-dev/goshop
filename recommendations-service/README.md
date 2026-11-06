# Recommendations Service

Відповідальність: підбір схожих, альтернативних і доповнюючих товарів на основі атрибутів, категорій, цінового діапазону та сумісності — без Machine Learning.

---

## Entities

### RecommendationRule
| Поле | Тип | Опис |
|---|---|---|
| `id` | `UUID` | PK, auto-generated |
| `name` | `String` | назва правила |
| `strategy` | `RecommendationStrategy` | `SIMILAR_ATTRIBUTES` / `SAME_CATEGORY` / `PRICE_RANGE` / `COMPATIBILITY_BASED` / `POPULAR_IN_CATEGORY` |
| `parameters` | `String?` | JSONB: параметри конкретної стратегії |
| `priority` | `Int` | пріоритет застосування правила |
| `enabled` | `Boolean` | чи активне правило |

### ProductRelation
| Поле | Тип | Опис |
|---|---|---|
| `id` | `UUID` | PK, auto-generated |
| `sourceProductId` | `UUID` | UUID товару-джерела (крос-сервісне посилання) |
| `targetProductId` | `UUID` | UUID рекомендованого товару |
| `relationType` | `RelationType` | `SIMILAR` / `ALTERNATIVE` / `ACCESSORY` / `UPGRADE` |
| `score` | `Double` | ваговий коефіцієнт релевантності (0.0–1.0) |

> Унікальний constraint: `(source_product_id, target_product_id, relation_type)`

### RecommendationSnapshot
| Поле | Тип | Опис |
|---|---|---|
| `id` | `UUID` | PK, auto-generated |
| `productId` | `UUID` | UUID товару, для якого збережено рекомендації |
| `strategy` | `RecommendationStrategy` | стратегія, за якою згенеровано snapshot |
| `recommendedProductIds` | `List<UUID>` | попередньо розраховані UUID рекомендованих товарів (окрема таблиця, з `position`) |
| `generatedAt` | `Instant` | час генерації snapshot |

---

## Endpoints

Базовий префікс: `/api`

### Recommendations
| Метод | Шлях | Опис |
|---|---|---|
| `GET` | `/api/recommendations/products/{id}` | загальні рекомендації для товару |
| `GET` | `/api/recommendations/products/{id}/similar` | схожі товари |
| `GET` | `/api/recommendations/products/{id}/alternatives` | альтернативи |
| `GET` | `/api/recommendations/products/{id}/accessories` | аксесуари |
| `POST` | `/api/recommendations/build` | рекомендації для збірки ПК |

### Rules
| Метод | Шлях | Опис |
|---|---|---|
| `GET` | `/api/recommendations/rules` | список правил рекомендацій |
| `POST` | `/api/recommendations/rules` | створити правило |
| `PUT` | `/api/recommendations/rules/{id}` | оновити правило |

---

## Configuration

| Змінна | За замовчуванням | Опис |
|---|---|---|
| `RECOMMENDATIONS_POSTGRES_HOST` | `localhost` | хост PostgreSQL |
| `RECOMMENDATIONS_POSTGRES_PORT` | `5432` | порт PostgreSQL |
| `RECOMMENDATIONS_POSTGRES_DB` | — | назва бази даних |
| `RECOMMENDATIONS_POSTGRES_USER` | — | користувач БД |
| `RECOMMENDATIONS_POSTGRES_PASSWORD` | — | пароль БД |
| `RECOMMENDATIONS_SERVER_PORT` | `8085` | порт сервісу |
| `RECOMMENDATIONS_API_PREFIX` | `/api` | префікс маршрутів |
| `RECOMMENDATIONS_SEED_ENABLE` | `false` | увімкнути завантаження початкових даних |
| `RECOMMENDATIONS_SEED_MODE` | `init` | режим seed (`init` / `prod`) |
| `RECOMMENDATIONS_SEED_STOP_COUNT` | `1` | пропустити seed якщо в таблиці вже є записи |

---

## Run

```bash
# Запуск PostgreSQL
docker compose up postgres-recommendations -d

# Запуск сервісу
./gradlew :recommendations-service:bootRun
```
