CRUD + бизнес-логика
Ответственность: хранение переводов названий, описаний и других локализуемых полей товаров.
Entities

Language(
code,
name,
enabled
)

Translation(
id,
entity_type,
entity_id,
field_name,
language_code,
translated_value,
status,
updated_at
)

TranslationJob(
id,
entity_type,
entity_id,
target_language,
status,
attempts,
created_at,
completed_at
)
Например:
entity_type = PRODUCT
entity_id = 123
field_name = name
language_code = uk
translated_value = Відеокарта NVIDIA RTX 5070

Endpoints
Method	Endpoint	Назначение
GET	/languages	Список языков
POST	/translations	Создание перевода
GET	/translations	Получение переводов с фильтрами
PUT	/translations/{id}	Изменение перевода
DELETE	/translations/{id}	Удаление
POST	/translations/resolve	Получение перевода с fallback
POST	/translation-jobs	Запуск задания перевода
GET	/translation-jobs/{id}	Статус задания

Бизнес-методы:
- translateProductName()
- translateProductDescription()
- resolveTranslation()
- fallbackToDefaultLanguage()
- processProductUpdatedEvent()
  Начнем с ручных переводов и словаря. Автоматический перевод через внешний AI/API добавим позже.

Не переводим торговые марки, SKU и другие идентификаторы. Новые переводы должны проходить валидацию, а автоматические — при необходимости проверку.