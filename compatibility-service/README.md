# Compatibility Service

Відповідальність: перевірка сумісності комплектуючих комп'ютера на основі правил (сокет CPU/MB, тип RAM, потужність БЖ, розміри відеокарти тощо).

---

## Entities

### CompatibilityRule
| Поле | Тип | Опис |
|---|---|---|
| `id` | `UUID` | PK, auto-generated |
| `code` | `String` | унікальний код правила, наприклад `CPU_MOTHERBOARD_SOCKET` |
| `sourceProductTypeId` | `UUID` | UUID типу товару-джерела (крос-сервісне посилання) |
| `targetProductTypeId` | `UUID` | UUID типу товару-цілі (крос-сервісне посилання) |
| `ruleType` | `RuleType` | `SOCKET_MATCH` / `RAM_COMPATIBILITY` / `POWER_CAPACITY` / `GPU_CLEARANCE` / `COOLING_TDP` / `FORM_FACTOR` |
| `parameters` | `String?` | JSONB: параметри для конкретного обробника правила |
| `severity` | `Severity` | `ERROR` / `WARNING` / `INFO` |
| `enabled` | `Boolean` | чи активне правило |
| `version` | `Int` | версія правила |

### CompatibilityCheck
| Поле | Тип | Опис |
|---|---|---|
| `id` | `UUID` | PK, auto-generated |
| `productIds` | `List<UUID>` | UUID товарів, що перевіряються разом (окрема таблиця `compatibility_check_products`) |
| `compatible` | `Boolean` | загальний результат перевірки |
| `checkedAt` | `Instant` | час проведення перевірки |

### CompatibilityIssue
| Поле | Тип | Опис |
|---|---|---|
| `id` | `UUID` | PK, auto-generated |
| `check` | `CompatibilityCheck` | перевірка, до якої належить проблема |
| `rule` | `CompatibilityRule` | правило, яке виявило проблему |
| `severity` | `Severity` | `ERROR` / `WARNING` / `INFO` |
| `messageCode` | `String` | i18n-ключ повідомлення, наприклад `SOCKET_MISMATCH` |

---

## Endpoints

Базовий префікс: `/api`

### Compatibility
| Метод | Шлях | Опис |
|---|---|---|
| `POST` | `/api/compatibility/check` | перевірити набір комплектуючих |
| `POST` | `/api/compatibility/pairs/check` | перевірити пару товарів |
| `GET` | `/api/compatibility/products/{id}` | знайти сумісні товари для конкретного товару |

### Rules
| Метод | Шлях | Опис |
|---|---|---|
| `GET` | `/api/compatibility/rules` | список усіх правил |
| `POST` | `/api/compatibility/rules` | створити правило |
| `PUT` | `/api/compatibility/rules/{id}` | оновити правило |
| `DELETE` | `/api/compatibility/rules/{id}` | вимкнути правило |

---

## Configuration

| Змінна | За замовчуванням | Опис |
|---|---|---|
| `COMPATIBILITY_POSTGRES_HOST` | `localhost` | хост PostgreSQL |
| `COMPATIBILITY_POSTGRES_PORT` | `5432` | порт PostgreSQL |
| `COMPATIBILITY_POSTGRES_DB` | — | назва бази даних |
| `COMPATIBILITY_POSTGRES_USER` | — | користувач БД |
| `COMPATIBILITY_POSTGRES_PASSWORD` | — | пароль БД |
| `COMPATIBILITY_SERVER_PORT` | `8082` | порт сервісу |
| `COMPATIBILITY_API_PREFIX` | `/api` | префікс маршрутів |
| `COMPATIBILITY_SEED_ENABLE` | `false` | увімкнути завантаження початкових даних |
| `COMPATIBILITY_SEED_MODE` | `init` | режим seed (`init` / `prod`) |
| `COMPATIBILITY_SEED_STOP_COUNT` | `1` | пропустити seed якщо в таблиці вже є записи |

---

## Run

```bash
# Запуск PostgreSQL
docker compose up postgres-compatibility -d

# Запуск сервісу
./gradlew :compatibility-service:bootRun
```
