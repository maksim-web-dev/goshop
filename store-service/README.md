CRUD
Ответственность: управление магазинами, которые подключаются к платформе, их API-доступом и настройками.
Entities
Store(
id,
name,
domain,
country_code,
default_language,
default_currency,
status,
created_at
)

StoreApiKey(
id,
store_id,
key_hash,
key_prefix,
status,
created_at,
expires_at,
last_used_at
)

StoreSettings(
id,
store_id,
requests_per_second,
burst_limit,
allowed_languages
)

StoreWebhook(
id,
store_id,
url,
event_types,
secret_ref,
enabled
)
Endpoints
Method	Endpoint	Назначение
GET	/stores	Список магазинов
GET	/stores/{id}	Информация
POST	/stores	Регистрация магазина
PATCH	/stores/{id}	Изменение
DELETE	/stores/{id}	Деактивация
POST	/stores/{id}/api-keys	Выпуск API Key
POST	/stores/{id}/api-keys/{keyId}/revoke	Отзыв ключа
GET	/stores/{id}/settings	Настройки
PUT	/stores/{id}/settings	Обновление настроек
POST	/stores/{id}/webhooks	Регистрация webhook

Бизнес-методы:
- authenticateStore()
- validateApiKey()
- rotateApiKey()
- getStoreLimits()
- deactivateStore()
  Секретный API Key показывается только при создании. В базе храним его безопасный хеш, а не открытый ключ.

Для высокой нагрузки api-service не должен обращаться к PostgreSQL магазина при каждом запросе. Проверки и лимиты можно кэшировать, с продуманным отзывом ключей и сроком действия кэша.