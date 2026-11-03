Recommendation Engine
Ответственность: подбор похожих, альтернативных и дополняющих товаров.

Для начала применяем алгоритмы на основе характеристик, категорий, цены и совместимости, без Machine Learning.
Entities
RecommendationRule(
id,
name,
strategy,
parameters,
priority,
enabled
)

ProductRelation(
id,
source_product_id,
target_product_id,
relation_type,
score
)

RecommendationSnapshot(
id,
product_id,
strategy,
recommended_product_ids,
generated_at
)
relation_type: SIMILAR, ALTERNATIVE, ACCESSORY, UPGRADE.
Endpoints
Method	Endpoint	Назначение
GET	/recommendations/products/{id}	Общие рекомендации
GET	/recommendations/products/{id}/similar	Похожие товары
GET	/recommendations/products/{id}/alternatives	Альтернативы
GET	/recommendations/products/{id}/accessories	Аксессуары
POST	/recommendations/build	Рекомендации для сборки ПК
GET	/recommendations/rules	Правила
POST	/recommendations/rules	Создание
PUT	/recommendations/rules/{id}	Изменение

Бизнес-методы:
- findSimilarProducts()
- findAlternativeProducts()
- recommendAccessories()
- rankRecommendations()
- generateRecommendationSnapshot()
- invalidateRecommendationsOnProductUpdate()
  Один из вариантов оптимизации: рассчитывать рекомендации заранее, хранить в Redis и обновлять по событиям Kafka.