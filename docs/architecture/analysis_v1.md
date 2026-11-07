# GoShop — план усунення проблем перед проєктуванням `api-service`

Дата аналізу: 2026-11-06. Джерело: `goshop-documentation_v1_11_06.zip` (7 README, 7 OpenAPI YAML, `architecture-review.md`, 20 JSON-файлів). **Обмеження:** вихідний Kotlin-код і реальна БД не входять в архів. Висновки щодо поведінки реалізації, наведені в `architecture-review.md`, потребують перевірки тестами або кодом.

## P0 — блокери інтеграції

1. **Узгодити ідентифікатори.** `product-service` має `Long` PK для Product/Brand/Category/ProductType; у `pricing`, `localization`, `compatibility`, `search`, `recommendations` міжсервісні посилання — `UUID`. Обрати одну стратегію. Рекомендований мінімально інвазивний варіант: зберегти внутрішні Long PK в `product-service`, додати стабільні UUID як зовнішні публічні ID для сутностей, на які посилаються інші сервіси, з унікальними індексами та міграцією, або перейти на Long у всіх міжсервісних контрактах. Для вже створених UUID seed-даних перший варіант зручніший. Після рішення синхронно оновити API DTO, БД, OpenAPI, seed-дані і тести; **не плутати внутрішній ID та зовнішній ID**.
2. **Виправити seed-категорії.** `product-service/data/init/product_category.json` має нульовий розмір і не є коректним JSON. `product_type.json` і `product_category_parameter.json` посилаються на `category_slug`, якого немає у seed-категоріях.
3. **Сформувати єдиний наскрізний набір даних.** Product seed: смартфони, ноутбуки, телевізор тощо. Search/pricing/localization/recommendations: GPU/CPU та інші ПК-компоненти з `11111111-...` UUID. Підготувати єдиний каталог PC hardware із зафіксованими зовнішніми ID, типами, категоріями, характеристиками, перекладами, цінами та зв'язками, а не незалежні фіктивні набори.
4. **Перевірити конфігурацію JPA.** `architecture-review.md` повідомляє про 
   `PostgresConfig.setPackagesToScan("zero44.crm.core.data.postgres")` у сервісах. 
  Підтвердити в коді; якщо вірно — виправити на фактичні packages або прибрати зайву custom-конфігурацію.
5. **Усунути функціональні заглушки.** За `architecture-review.md`, `CompatibilityCheckService.evaluateRule()` завжди повертає `null`, `findCompatibleProducts()` — `emptyList()`, `search-service` ігнорує `minPrice/maxPrice`. Перевірити кодом та тестами; не включати ці можливості в інтеграцію як робочі до виправлення.

## P1 — мінімальні контракти для майбутнього `api-service`

6. **Додати batch API каталогу.** `POST /api/products/batch` із `productIds`, обмеженням розміру (наприклад, 100) і відповіддю з елементами та списком не знайдених ID. Уникати N+1 під час рекомендацій і пошуку.
7. **Додати batch resolve перекладів.** `POST /api/translations/batch-resolve` для множини `(entityType, entityId, fieldName, language)` із fallback і явним маркером, звідки взято текст. Обмежити розмір запиту.
8. **Використати наявний `POST /api/prices/batch`.** Перевірити контракт, чи дозволяє він отримувати ефективні ціни з урахуванням `storeId`, валюти, скидок, невідомих ID і округлення. Якщо ні — доповнити або додати calculate-batch.
9. **Привести до спільного стандарту контракти помилок.** Один ErrorResponse (`code`, `message`, `traceId`, `details`), статуси 400/401/403/404/409/422/429/503, Bean Validation у DTO, обмеження розмірів `pageSize`, `productIds`, довжини рядків.
10. **Додати pagination і контрольоване сортування** для необмежених GET-списків (`brands`, `translations`, `discounts` та ін.). Максимальні page size, стабільне сортування, допустимі поля сортування.
11. **Описати внутрішні та зовнішні endpoints.** Публічні B2B-запити лише через `api-service`; CRUD адміністрування — окрема довірена зона; `/api/internal/*` недоступний через публічний ingress.

## P1 — тестові дані та перевірки

12. Зробити машиночитану `seed-manifest.json` з природними ключами/UUID, порядком завантаження і версією набору, або детерміновані Flyway seed-міграції лише для local/test. Не використовувати випадково згенеровані PK як міжсервісні посилання.
13. Послідовність: stores + languages → brands/categories/product types/attributes/products → prices/translations/relations/compatibility rules → search projection. Додавати перевірки посилальної цілісності **на рівні integration test**, бо FK між окремими БД немає.
14. Сценарії наскрізної перевірки: GPU з локалізацією та магазинною ціною; CPU+motherboard same/different socket; відсутній товар; знижка; archived product; пошук; recommendations; відмова одного downstream.
15. Перевірити **всі 20 JSON-файлів** парсером (19 валідних, один порожній у цьому ZIP); далі — schema validation, referential integrity, enum/value constraints і відповідність полів фактичним імпортерам.
16. Зафіксувати, що продакшн-датасет є **окремим подальшим завданням**: імпорт із ліцензованих джерел, нормалізація, якість даних, дедуплікація, оновлення й provenance. Тестовий набір не слід називати production-ready.

## P2 — підготовка до навантаження і Kubernetes

17. В усі сім сервісів додати Actuator + readiness/liveness/startup probes, graceful shutdown, обмеження часу HTTP-запитів та правила retry з backoff лише для безпечних/ідемпотентних операцій.
18. Додати request/trace IDs, структуровані логи, Micrometer + Prometheus та OpenTelemetry. Для першої baseline-сесії вимірювати RPS, p95/p99, error rate, CPU/RAM, DB pool usage і cache hit ratio.
19. Перевірити індекси на реальних WHERE/JOIN/ORDER BY, FTS GIN-індекси і плани `EXPLAIN ANALYZE`; налаштувати пул з урахуванням кількості pod-реплік, а не лише одного контейнера.
20. Впровадити контроль доступу: API key із `store-service`, кешування перевірок з безпечним відкликанням; окреме адміністрування; внутрішня автентифікація між сервісами; rate limiting per-store з глобально узгодженим лімітом.
21. Запроєктувати асинхронну синхронізацію даних: `product.created/updated/archived`, `price.updated`, `translation.updated`, `store.deactivated`; після цього Kafka consumers, retry/DLQ, ідемпотентність, Outbox. Поточні інтеграції в документації не реалізовані.
22. Перевірити контейнеризацію: збірка Gradle з кешем, resource requests/limits, безпечні secrets, ConfigMaps, Deployment/Service, zero-downtime rolling update та rollback. `api-service` поки stub, його deployment не перевірений.

## Критерії готовності до проєктування `api-service`

- Один узгоджений зовнішній ID для кожного Product/Brand/Category/ProductType у всіх сервісах.
- Seed-дані проходять JSON/schema validation та integration checks, є хоча б одна наскрізна PC-конфігурація.
- Працюють product batch, translation batch, pricing batch; зрозумілі response/error contracts і максимальні розміри запитів.
- Реалізовані та протестовані критичні бізнес-методи (або явно прибрані з MVP-контракту).
- Визначені правила auth, timeouts, fallbacks, pagination; є health endpoints.
- OpenAPI YAML оновлені відповідно до фактичних контролерів і DTO.

## Рекомендований порядок виконання

**Спринт A — модель та seed:** пункти 1–5, 12–16. 
**Спринт B — інтеграційні контракти:** пункти 6–11. 
**Спринт C — експлуатаційна готовність:** пункти 17–22 (частину можна робити паралельно). 
**Після A+B** — проєктування та MVP `api-service`, не чекаючи повного завершення C.

## Примітка про рівень достовірності

Безпосередньо перевірено структуру ZIP, наявність і синтаксичну коректність JSON, приклади полів seed-даних. Деталі коду наведені на основі `architecture-review.md` і потребують повторного підтвердження на рівні source/tests. Наявність OpenAPI YAML не гарантує фактичної відповідності поточному runtime.
