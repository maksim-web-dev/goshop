Предлагаю использовать общий префикс /api/v1, UUID для идентификаторов и Instant для временных меток, хранящихся в PostgreSQL как timestamptz.

Вначале опишем полную целевую модель, а реализовывать будем последовательно.
3.1. products-service — Product Catalog Service
CRUD
Ответственность: хранение компьютерной техники, категорий, производителей, типов товаров и технических характеристик.
Entities:
Product(id,sku,name,brand_id,category_id,type_id,description,status,created_at,updated_at)
Brand(id,name,website_url)
Category(id,parent_id,name,slug)
ProductType(id,category_id,name)
AttributeDefinition(id,type_id,code,name,data_type,unit,filterable)
ProductAttribute(id,product_id,attribute_definition_id,value)
ProductImage(id,product_id,image_url,sort_order)

Примеры атрибутов: 
cpu_socket, 
ram_capacity, 
gpu_memory, 
power_consumption, 
screen_size.

Для первой версии value может быть текстовым или JSONB. 
Позже стоит добавить строгую типизацию и валидацию значений по data_type.

Endpoints
Method	Endpoint	Назначение
GET	/products	Список товаров, фильтры, пагинация
GET	/products/{id}	Получение товара
POST	/products	Создание
PATCH	/products/{id}	Обновление
DELETE	/products/{id}	Архивирование
GET	/categories	Категории
POST	/categories	Создание категории
GET	/brands	Бренды
POST	/brands	Создание бренда
GET	/product-types	Типы товаров
GET	/product-types/{id}/attributes	Схема характеристик
PUT	/products/{id}/attributes	Обновление характеристик

Для категорий, брендов, типов и схем атрибутов также понадобятся 
стандартные GET /{id}, PATCH /{id} и DELETE /{id} административные операции.

Бизнес-методы:
- findProductsByCategory()
- findProductsByAttributes()
- getProductDetails()
- publishProductUpdatedEvent()
  Kafka events: ProductCreated, ProductUpdated, ProductDeleted.

Цены в этом сервисе не храним.