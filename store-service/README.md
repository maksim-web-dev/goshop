# Store Service

Відповідальність: управління магазинами, що підключаються до платформи, їх API-ключами, налаштуваннями та webhook-підписками.

---

## Entities

### Store
| Поле | Тип | Опис |
|---|---|---|
| `id` | `UUID` | PK, auto-generated |
| `name` | `String` | назва магазину |
| `domain` | `String` | унікальний домен магазину |
| `countryCode` | `String` | ISO 3166-1 alpha-2 код країни |
| `defaultLanguage` | `String` | BCP 47 код мови за замовчуванням |
| `defaultCurrency` | `String` | ISO 4217 код валюти за замовчуванням |
| `status` | `StoreStatus` | `ACTIVE` / `INACTIVE` / `SUSPENDED` |
| `createdAt` | `Instant` | дата реєстрації |

### StoreApiKey
| Поле | Тип | Опис |
|---|---|---|
| `id` | `UUID` | PK, auto-generated |
| `store` | `Store` | магазин, якому належить ключ |
| `keyHash` | `String` | bcrypt-хеш ключа; відкритий ключ не зберігається |
| `keyPrefix` | `String` | перші 8 символів ключа для відображення в UI |
| `status` | `ApiKeyStatus` | `ACTIVE` / `REVOKED` / `EXPIRED` |
| `createdAt` | `Instant` | дата видачі ключа |
| `expiresAt` | `Instant?` | термін дії; `null` = безстроковий |
| `lastUsedAt` | `Instant?` | час останнього використання |

### StoreSettings
| Поле | Тип | Опис |
|---|---|---|
| `id` | `UUID` | PK, auto-generated |
| `store` | `Store` | магазин (унікальний зв'язок OneToOne) |
| `requestsPerSecond` | `Int` | ліміт запитів на секунду |
| `burstLimit` | `Int` | максимальний burst |
| `allowedLanguages` | `List<String>` | BCP 47 коди дозволених мов (окрема таблиця `store_allowed_languages`) |

### StoreWebhook
| Поле | Тип | Опис |
|---|---|---|
| `id` | `UUID` | PK, auto-generated |
| `store` | `Store` | магазин, якому належить webhook |
| `url` | `String` | URL endpoint для отримання подій |
| `eventTypes` | `List<String>` | типи подій підписки (окрема таблиця `store_webhook_event_types`) |
| `secretRef` | `String` | посилання на секрет у Vault/Secrets Manager |
| `enabled` | `Boolean` | чи активний webhook |

---

## Endpoints

Базовий префікс: `/api`

### Stores
| Метод | Шлях | Опис |
|---|---|---|
| `GET` | `/api/stores` | список магазинів |
| `GET` | `/api/stores/{id}` | інформація про магазин |
| `POST` | `/api/stores` | зареєструвати магазин |
| `PATCH` | `/api/stores/{id}` | оновити дані магазину |
| `DELETE` | `/api/stores/{id}` | деактивувати магазин |

### API Keys
| Метод | Шлях | Опис |
|---|---|---|
| `POST` | `/api/stores/{id}/api-keys` | видати новий API-ключ (повертає відкритий ключ лише один раз) |
| `POST` | `/api/stores/{id}/api-keys/{keyId}/revoke` | відкликати API-ключ |

### Settings
| Метод | Шлях | Опис |
|---|---|---|
| `GET` | `/api/stores/{id}/settings` | налаштування магазину |
| `PUT` | `/api/stores/{id}/settings` | оновити налаштування |

### Webhooks
| Метод | Шлях | Опис |
|---|---|---|
| `POST` | `/api/stores/{id}/webhooks` | зареєструвати webhook |

---

## Configuration

| Змінна | За замовчуванням | Опис |
|---|---|---|
| `STORE_POSTGRES_HOST` | `localhost` | хост PostgreSQL |
| `STORE_POSTGRES_PORT` | `5432` | порт PostgreSQL |
| `STORE_POSTGRES_DB` | — | назва бази даних |
| `STORE_POSTGRES_USER` | — | користувач БД |
| `STORE_POSTGRES_PASSWORD` | — | пароль БД |
| `STORE_SERVER_PORT` | `8087` | порт сервісу |
| `STORE_API_PREFIX` | `/api` | префікс маршрутів |
| `STORE_SEED_ENABLE` | `false` | увімкнути завантаження початкових даних |
| `STORE_SEED_MODE` | `init` | режим seed (`init` / `prod`) |
| `STORE_SEED_STOP_COUNT` | `1` | пропустити seed якщо в таблиці вже є записи |

---

## Run

```bash
# Запуск PostgreSQL
docker compose up postgres-store -d

# Запуск сервісу
./gradlew :store-service:bootRun
```
