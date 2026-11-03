Rules Engine
Ответственность: определение совместимости комплектующих компьютера.

Например: совместим ли Intel Core i7 с выбранной материнской платой? Хватит ли мощности блока питания? Поместится ли видеокарта в корпус?
Entities
CompatibilityRule(
id,
code,
source_product_type,
target_product_type,
rule_type,
parameters,
severity,
enabled,
version
)

CompatibilityCheck(
id,
product_ids,
compatible,
checked_at
)

CompatibilityIssue(
id,
check_id,
rule_id,
severity,
message_code
)
parameters можно хранить как JSONB. В первую очередь разработаем строгие типизированные обработчики правил, а не универсальный интерпретатор произвольных выражений.

CompatibilityCheck и CompatibilityIssue необязательно сохранять для каждого запроса: при высокой нагрузке проверка может быть stateless, а история — опциональной.
Endpoints
Method	Endpoint	Назначение
POST	/compatibility/check	Проверить набор комплектующих
POST	/compatibility/pairs/check	Проверить пару
GET	/compatibility/products/{id}	Найти совместимые товары
GET	/compatibility/rules	Список правил
POST	/compatibility/rules	Создание
PUT	/compatibility/rules/{id}	Изменение
DELETE	/compatibility/rules/{id}	Отключение правила

Бизнес-методы:
- checkCpuMotherboardSocket()
- checkRamCompatibility()
- checkPowerSupplyCapacity()
- checkGpuCaseClearance()
- validatePcConfiguration()
- explainCompatibilityIssues()
  Здесь будет интересно реализовать Strategy Pattern, Specification Pattern, а также сравнить последовательную и параллельную обработку правил.