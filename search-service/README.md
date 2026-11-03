Read-oriented
Ответственность: быстрый полнотекстовый поиск, фильтрация, сортировка и выдача результатов.

Сервис не должен быть владельцем товаров. Он хранит поисковую проекцию каталога и обновляет ее на основании событий.
Models
SearchDocument(
product_id,
sku,
name,
localized_names,
brand,
category_id,
attributes,
searchable_text,
status,
indexed_at
)

SearchIndexJob(
id,
product_id,
operation,
status,
attempts,
created_at
)
SearchDocument — модель поискового документа, не обязательно JPA Entity.
Endpoints
Method	Endpoint	Назначение
GET	/search/products	Полнотекстовый поиск
POST	/search/products/query	Сложные фильтры
GET	/search/suggestions	Автодополнение
GET	/search/facets	Доступные фильтры и количества
POST	/internal/search/reindex	Переиндексация
GET	/internal/search/index-jobs/{id}	Статус индексации

Бизнес-методы:
- indexProduct()
- updateProductIndex()
- removeProductFromIndex()
- searchByKeywords()
- filterByAttributes()
- rankSearchResults()
  Пример:
  GET /api/v1/search/products?q=RTX&brand=NVIDIA&minPrice=300


Ценовые фильтры потребуют согласованной с pricing-service поисковой проекции, учитывающей магазин, валюту и актуальность цены.

Начальный вариант: PostgreSQL full-text search. Позже — OpenSearch, сравнение производительности и нагрузки.