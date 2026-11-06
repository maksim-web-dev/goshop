# Localization Service

Відповідальність: зберігання та видача перекладів назв, описів та інших локалізованих полів товарів, брендів і категорій.

---

## Entities

### Language
| Поле | Тип | Опис |
|---|---|---|
| `code` | `String` | PK, BCP 47 код мови, наприклад `uk`, `en`, `de` |
| `name` | `String` | повна назва мови |
| `enabled` | `Boolean` | чи активна мова |

### Translation
| Поле | Тип | Опис |
|---|---|---|
| `id` | `UUID` | PK, auto-generated |
| `entityType` | `EntityType` | `PRODUCT` / `BRAND` / `CATEGORY` / `PRODUCT_TYPE` |
| `entityId` | `UUID` | UUID сутності з відповідного сервісу (крос-сервісне посилання) |
| `fieldName` | `String` | назва поля, наприклад `name`, `description` |
| `language` | `Language` | мова перекладу |
| `translatedValue` | `String` | текст перекладу (TEXT) |
| `status` | `TranslationStatus` | `DRAFT` / `APPROVED` / `AUTO_TRANSLATED` |
| `updatedAt` | `Instant` | дата останнього оновлення |

> Унікальний constraint: `(entity_type, entity_id, field_name, language_code)`

### TranslationJob
| Поле | Тип | Опис |
|---|---|---|
| `id` | `UUID` | PK, auto-generated |
| `entityType` | `EntityType` | `PRODUCT` / `BRAND` / `CATEGORY` / `PRODUCT_TYPE` |
| `entityId` | `UUID` | UUID сутності, яку потрібно перекласти |
| `targetLanguage` | `Language` | цільова мова перекладу |
| `status` | `TranslationJobStatus` | `PENDING` / `IN_PROGRESS` / `DONE` / `FAILED` |
| `attempts` | `Int` | кількість спроб виконання |
| `createdAt` | `Instant` | дата створення завдання |
| `completedAt` | `Instant?` | дата завершення завдання |

---

## Endpoints

Базовий префікс: `/api`

### Languages
| Метод | Шлях | Опис |
|---|---|---|
| `GET` | `/api/languages` | список усіх мов |

### Translations
| Метод | Шлях | Опис |
|---|---|---|
| `GET` | `/api/translations` | список перекладів (фільтри: `entityType`, `entityId`, `languageCode`) |
| `POST` | `/api/translations` | створити переклад |
| `PUT` | `/api/translations/{id}` | оновити переклад |
| `DELETE` | `/api/translations/{id}` | видалити переклад |
| `POST` | `/api/translations/resolve` | отримати переклад з fallback на мову за замовчуванням |

### Translation Jobs
| Метод | Шлях | Опис |
|---|---|---|
| `POST` | `/api/translation-jobs` | запустити завдання перекладу |
| `GET` | `/api/translation-jobs/{id}` | статус завдання перекладу |

---

## Configuration

| Змінна | За замовчуванням | Опис |
|---|---|---|
| `LOCALIZATION_POSTGRES_HOST` | `localhost` | хост PostgreSQL |
| `LOCALIZATION_POSTGRES_PORT` | `5432` | порт PostgreSQL |
| `LOCALIZATION_POSTGRES_DB` | — | назва бази даних |
| `LOCALIZATION_POSTGRES_USER` | — | користувач БД |
| `LOCALIZATION_POSTGRES_PASSWORD` | — | пароль БД |
| `LOCALIZATION_SERVER_PORT` | `8083` | порт сервісу |
| `LOCALIZATION_API_PREFIX` | `/api` | префікс маршрутів |
| `LOCALIZATION_SEED_ENABLE` | `false` | увімкнути завантаження початкових даних |
| `LOCALIZATION_SEED_MODE` | `init` | режим seed (`init` / `prod`) |
| `LOCALIZATION_SEED_STOP_COUNT` | `1` | пропустити seed якщо в таблиці вже є записи |

---

## Run

```bash
# Запуск PostgreSQL
docker compose up postgres-localization -d

# Запуск сервісу
./gradlew :localization-service:bootRun
```
