# Search Service

Відповідальність: повнотекстовий пошук, фільтрація та сортування товарів. Сервіс не є власником каталогу — зберігає пошукову проекцію та оновлює її за подіями.

---

## Models

### SearchDocument
> Не JPA entity — пошукова проекція каталогу (PostgreSQL FTS).

| Поле | Тип | Опис |
|---|---|---|
| `productId` | `UUID` | UUID товару з product-service |
| `sku` | `String` | артикул товару |
| `name` | `String` | назва товару |
| `localizedNames` | `Map<String, String>` | JSONB: локалізовані назви `{"uk": "...", "en": "..."}` |
| `brand` | `String` | назва бренду |
| `categoryId` | `UUID` | UUID категорії |
| `attributes` | `Map<String, String>` | JSONB: атрибути для фільтрації |
| `searchableText` | `String` | поле `tsvector` для FTS (PostgreSQL) |
| `status` | `String` | статус товару |
| `indexedAt` | `Instant` | час останньої індексації |

### SearchIndexJob
| Поле | Тип | Опис |
|---|---|---|
| `id` | `UUID` | PK, auto-generated |
| `productId` | `UUID` | UUID товару, що індексується |
| `operation` | `IndexOperation` | `INDEX` / `UPDATE` / `DELETE` |
| `status` | `IndexJobStatus` | `PENDING` / `IN_PROGRESS` / `DONE` / `FAILED` |
| `attempts` | `Int` | кількість спроб виконання |
| `createdAt` | `Instant` | дата створення завдання |

---

## Endpoints

Базовий префікс: `/api`

### Search
| Метод | Шлях | Опис |
|---|---|---|
| `GET` | `/api/search/products` | повнотекстовий пошук товарів (`q`, `brand`, `categoryId`, `status`) |
| `POST` | `/api/search/products/query` | пошук зі складними фільтрами |
| `GET` | `/api/search/suggestions` | автодоповнення за префіксом запиту |

### Internal
| Метод | Шлях | Опис |
|---|---|---|
| `POST` | `/api/internal/search/reindex` | переіндексувати товар |
| `GET` | `/api/internal/search/index-jobs/{id}` | статус завдання індексації |

---

## Configuration

| Змінна | За замовчуванням | Опис |
|---|---|---|
| `SEARCH_POSTGRES_HOST` | `localhost` | хост PostgreSQL |
| `SEARCH_POSTGRES_PORT` | `5432` | порт PostgreSQL |
| `SEARCH_POSTGRES_DB` | — | назва бази даних |
| `SEARCH_POSTGRES_USER` | — | користувач БД |
| `SEARCH_POSTGRES_PASSWORD` | — | пароль БД |
| `SEARCH_SERVER_PORT` | `8086` | порт сервісу |
| `SEARCH_API_PREFIX` | `/api` | префікс маршрутів |
| `SEARCH_SEED_ENABLE` | `false` | увімкнути завантаження початкових даних |
| `SEARCH_SEED_MODE` | `init` | режим seed (`init` / `prod`) |
| `SEARCH_SEED_STOP_COUNT` | `1` | пропустити seed якщо в таблиці вже є записи |

---

## Run

```bash
# Запуск PostgreSQL
docker compose up postgres-search -d

# Запуск сервісу
./gradlew :search-service:bootRun
```
