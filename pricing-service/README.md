CRUD + вычисления
Ответственность: цены, скидки, валюты, история изменения цен и специфические прайс-листы магазинов.
Entities
Price(
id,
product_id,
store_id,
amount,
currency_code,
valid_from,
valid_to,
updated_at
)

Discount(
id,
store_id,
product_id,
discount_type,
discount_value,
starts_at,
ends_at,
status
)

ExchangeRate(
id,
from_currency,
to_currency,
rate,
effective_at
)

PriceHistory(
id,
price_id,
old_amount,
new_amount,
changed_at
)
store_id в Price допускает null для базовой цены платформы. discount_type: PERCENTAGE или FIXED.
Endpoints
Method	Endpoint	Назначение
GET	/prices/products/{productId}	Актуальные цены
POST	/prices	Создание
PUT	/prices/{id}	Изменение
GET	/prices/{id}/history	История
POST	/prices/batch	Получение цен для списка товаров
POST	/prices/calculate	Итоговая цена со скидкой
POST	/discounts	Создание скидки
GET	/discounts	Список скидок
PUT	/discounts/{id}	Изменение скидки
DELETE	/discounts/{id}	Отключение скидки
GET	/exchange-rates	Курсы валют

Бизнес-методы:
- calculateFinalPrice()
- applyDiscount()
- convertCurrency()
- getEffectivePrice()
- publishPriceChangedEvent()
  Используем BigDecimal, не Double. Нужно заранее определить правила округления, выбор между базовой и магазинной ценой и приоритет скидок.