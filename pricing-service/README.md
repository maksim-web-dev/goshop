# Pricing Service

Відповідальність: зберігання цін, знижок, курсів валют та історії зміни цін. Розрахунок фінальної вартості з урахуванням знижок і прив'язки до конкретного магазину.

---

## Entities

### Price
| Поле | Тип | Опис |
|---|---|---|
| `id` | `UUID` | PK, auto-generated |
| `productId` | `UUID` | UUID товару з product-service |
| `storeId` | `UUID?` | UUID магазину; `null` = базова ціна платформи |
| `amount` | `BigDecimal` | сума (precision=19, scale=4) |
| `currencyCode` | `String` | ISO 4217 код валюти, наприклад `USD` |
| `validFrom` | `Instant` | початок дії ціни |
| `validTo` | `Instant?` | кінець дії ціни; `null` = безстрокова |
| `updatedAt` | `Instant` | дата останнього оновлення |

### Discount
| Поле | Тип | Опис |
|---|---|---|
| `id` | `UUID` | PK, auto-generated |
| `storeId` | `UUID` | UUID магазину, до якого застосовується знижка |
| `productId` | `UUID?` | UUID конкретного товару; `null` = знижка на весь асортимент |
| `discountType` | `DiscountType` | `PERCENTAGE` / `FIXED` |
| `discountValue` | `BigDecimal` | значення знижки (для PERCENTAGE: 0–100; для FIXED: сума) |
| `startsAt` | `Instant` | початок дії знижки |
| `endsAt` | `Instant?` | кінець дії знижки; `null` = безстрокова |
| `status` | `DiscountStatus` | `ACTIVE` / `INACTIVE` / `EXPIRED` |

### ExchangeRate
| Поле | Тип | Опис |
|---|---|---|
| `id` | `UUID` | PK, auto-generated |
| `fromCurrency` | `String` | ISO 4217 код вихідної валюти |
| `toCurrency` | `String` | ISO 4217 код цільової валюти |
| `rate` | `BigDecimal` | курс (precision=19, scale=8) |
| `effectiveAt` | `Instant` | дата актуальності курсу |

### PriceHistory
| Поле | Тип | Опис |
|---|---|---|
| `id` | `UUID` | PK, auto-generated |
| `price` | `Price` | запис ціни, до якого належить зміна |
| `oldAmount` | `BigDecimal` | попереднє значення |
| `newAmount` | `BigDecimal` | нове значення |
| `changedAt` | `Instant` | дата зміни |

---

## Endpoints

Базовий префікс: `/api`

### Prices
| Метод | Шлях | Опис |
|---|---|---|
| `GET` | `/api/prices/products/{productId}` | актуальні ціни товару (базова + усі магазини) |
| `POST` | `/api/prices` | створити ціну |
| `PUT` | `/api/prices/{id}` | оновити ціну (автоматично записує в PriceHistory) |
| `GET` | `/api/prices/{id}/history` | історія змін ціни |
| `POST` | `/api/prices/batch` | отримати ціни для списку товарів |
| `POST` | `/api/prices/calculate` | розрахувати фінальну ціну з урахуванням знижок |

### Discounts
| Метод | Шлях | Опис |
|---|---|---|
| `GET` | `/api/discounts` | список знижок (фільтри: `storeId`, `productId`) |
| `POST` | `/api/discounts` | створити знижку |
| `PUT` | `/api/discounts/{id}` | оновити знижку |
| `DELETE` | `/api/discounts/{id}` | вимкнути знижку |

### Exchange Rates
| Метод | Шлях | Опис |
|---|---|---|
| `GET` | `/api/exchange-rates` | останні актуальні курси валют |

---

## Configuration

| Змінна | За замовчуванням | Опис |
|---|---|---|
| `PRICING_POSTGRES_HOST` | `localhost` | хост PostgreSQL |
| `PRICING_POSTGRES_PORT` | `5432` | порт PostgreSQL |
| `PRICING_POSTGRES_DB` | — | назва бази даних |
| `PRICING_POSTGRES_USER` | — | користувач БД |
| `PRICING_POSTGRES_PASSWORD` | — | пароль БД |
| `PRICING_SERVER_PORT` | `8084` | порт сервісу |
| `PRICING_API_PREFIX` | `/api` | префікс маршрутів |
| `PRICING_SEED_ENABLE` | `false` | увімкнути завантаження початкових даних |
| `PRICING_SEED_MODE` | `init` | режим seed (`init` / `prod`) |
| `PRICING_SEED_STOP_COUNT` | `1` | пропустити seed якщо в таблиці вже є записи |

---

## Run

```bash
# Запуск PostgreSQL
docker compose up postgres-pricing -d

# Запуск сервісу
./gradlew :pricing-service:bootRun
```
