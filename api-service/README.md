API Aggregator
Ответственность: единая точка входа для магазинов и объединение данных нескольких микросервисов.

Это BFF/API composition service, а не хранилище общего каталога.
Models
ProductQueryRequest(
product_ids,
language_code,
store_id,
currency_code,
include_prices,
include_compatibility
)

ProductResponse(
product_id,
name,
description,
brand,
attributes,
price,
currency,
compatibility
)

ApiRequestContext(
request_id,
store_id,
language_code,
correlation_id
)
Эти модели — DTO, не JPA Entities. Постоянная собственная БД пока не нужна.
Endpoints
Method	Endpoint	Назначение
GET	/catalog/products/{id}	Агрегированная карточка товара
GET	/catalog/products	Каталог магазина
POST	/catalog/products/batch	Получение множества товаров
GET	/catalog/search	Поиск с ценами и локализацией
GET	/catalog/products/{id}/recommendations	Рекомендации
POST	/catalog/compatibility/check	Проверка совместимости

Бизнес-методы:
- validateClientRequest()
- aggregateProductDetails()
- resolveProductLocalization()
- enrichProductsWithPrices()
- batchFetchProducts()
- applyTimeoutAndFallback()
- applyRateLimit()
  Пример сценария: магазин запрашивает 50 видеокарт на украинском языке с ценами в гривнах. api-service параллельно собирает данные из необходимых сервисов и формирует один ответ.

Для будущих экспериментов со сверхвысокой нагрузкой именно этот сервис — один из главных кандидатов на реализацию сначала на Kotlin/Spring, затем на Go.