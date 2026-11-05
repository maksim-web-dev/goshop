# Product Service

Відповідальність: зберігання каталогу товарів, категорій, брендів, типів товарів, технічних характеристик та компонентів.

---

## Entities

### Brand
| Поле | Тип | Опис |
|---|---|---|
| `id` | `Long` | PK, auto-generated |
| `name` | `String` | унікальна назва бренду |
| `websiteUrl` | `String?` | посилання на сайт |

### Category
| Поле | Тип | Опис |
|---|---|---|
| `id` | `Long` | PK, auto-generated |
| `parent` | `Category?` | батьківська категорія (self-ref) |
| `name` | `String` | назва |
| `slug` | `String` | унікальний URL-ідентифікатор |

### ProductType
| Поле | Тип | Опис |
|---|---|---|
| `id` | `Long` | PK, auto-generated |
| `category` | `Category` | категорія, до якої належить тип |
| `name` | `String` | назва типу |

### Product
| Поле | Тип | Опис |
|---|---|---|
| `id` | `Long` | PK, auto-generated |
| `sku` | `String` | унікальний артикул |
| `name` | `String` | назва товару |
| `brand` | `Brand` | бренд |
| `category` | `Category` | категорія |
| `productType` | `ProductType` | тип товару |
| `description` | `String?` | опис (TEXT) |
| `status` | `ProductStatus` | `DRAFT` / `ACTIVE` / `ARCHIVED` |
| `createdAt` | `Instant` | дата створення |
| `updatedAt` | `Instant` | дата оновлення |

### ParameterGroup
| Поле | Тип | Опис |
|---|---|---|
| `id` | `Long` | PK, auto-generated |
| `name` | `String` | назва групи (наприклад: Color, Size) |

### ParameterGroupItem
| Поле | Тип | Опис |
|---|---|---|
| `id` | `Long` | PK, auto-generated |
| `name` | `String` | значення (наприклад: Red, XL) |
| `parameterGroup` | `ParameterGroup` | група, до якої належить елемент |

### CategoryParameter
| Поле | Тип | Опис |
|---|---|---|
| `id` | `Long` | PK, auto-generated |
| `name` | `String` | назва параметру |
| `order` | `Int` | порядок сортування |
| `category` | `Category` | категорія |
| `parameterType` | `ParameterType` | `INT` / `DOUBLE_2` / `DOUBLE_3` / `STRING` / `BOOLEAN` / `LIST` |
| `intValue` | `Int?` | значення для типу INT |
| `doubleValue2` | `Int?` | ціла частина для DOUBLE_2 |
| `doubleValue3` | `Int?` | ціла частина для DOUBLE_3 |
| `booleanValue` | `Boolean?` | значення для типу BOOLEAN |
| `stringValue` | `String?` | значення для типу STRING |
| `parameterGroup` | `ParameterGroup?` | група значень для типу LIST |

### ProductAttribute
| Поле | Тип | Опис |
|---|---|---|
| `id` | `Long` | PK, auto-generated |
| `product` | `Product` | товар |
| `categoryParameter` | `CategoryParameter` | визначення характеристики |
| `value` | `String` | значення (TEXT) |

> Унікальний constraint: `(product_id, category_parameter_id)`

### ComponentGroup
| Поле | Тип | Опис |
|---|---|---|
| `id` | `Long` | PK, auto-generated |
| `name` | `String` | назва групи (наприклад: Processor, Memory) |

### Component
| Поле | Тип | Опис |
|---|---|---|
| `id` | `Long` | PK, auto-generated |
| `name` | `String` | назва компонента |
| `componentGroup` | `ComponentGroup` | група компонентів |

---

## Endpoints

Базовий префікс: `/api`

### Brands
| Метод | Шлях | Опис |
|---|---|---|
| `GET` | `/api/brands` | список усіх брендів |
| `GET` | `/api/brands/{id}` | бренд за ID |
| `POST` | `/api/brands` | створити бренд |
| `PATCH` | `/api/brands/{id}` | оновити бренд |
| `DELETE` | `/api/brands/{id}` | видалити бренд |

### Categories
| Метод | Шлях | Опис |
|---|---|---|
| `GET` | `/api/categories` | список усіх категорій |
| `GET` | `/api/categories/{id}` | категорія за ID |
| `POST` | `/api/categories` | створити категорію |
| `PATCH` | `/api/categories/{id}` | оновити категорію |
| `DELETE` | `/api/categories/{id}` | видалити категорію |

### Product Types
| Метод | Шлях | Опис |
|---|---|---|
| `GET` | `/api/product-types` | список усіх типів товарів |
| `GET` | `/api/product-types/{id}` | тип за ID |
| `GET` | `/api/product-types/{id}/attributes` | схема характеристик типу (CategoryParameter[]) |
| `POST` | `/api/product-types` | створити тип |
| `PATCH` | `/api/product-types/{id}` | оновити тип |
| `DELETE` | `/api/product-types/{id}` | видалити тип |

### Products
| Метод | Шлях | Опис |
|---|---|---|
| `GET` | `/api/products` | список товарів (пагінація; фільтри: `categoryId`, `status`) |
| `GET` | `/api/products/{id}` | товар за ID |
| `POST` | `/api/products` | створити товар |
| `PATCH` | `/api/products/{id}` | оновити товар |
| `DELETE` | `/api/products/{id}` | архівувати товар (→ `ARCHIVED`) |

### Product Attributes
| Метод | Шлях | Опис |
|---|---|---|
| `GET` | `/api/products/{productId}/attributes` | всі характеристики товару |
| `PUT` | `/api/products/{productId}/attributes` | замінити всі характеристики товару |

### Category Parameters
| Метод | Шлях | Опис |
|---|---|---|
| `GET` | `/api/category-parameters?categoryId={id}` | параметри за категорією |
| `GET` | `/api/category-parameters/{id}` | параметр за ID |
| `POST` | `/api/category-parameters` | створити параметр |
| `PATCH` | `/api/category-parameters/{id}` | оновити параметр |
| `DELETE` | `/api/category-parameters/{id}` | видалити параметр |

### Parameter Groups
| Метод | Шлях | Опис |
|---|---|---|
| `GET` | `/api/parameter-groups` | список усіх груп |
| `GET` | `/api/parameter-groups/{id}` | група за ID |
| `GET` | `/api/parameter-groups/{id}/items` | елементи групи |
| `POST` | `/api/parameter-groups` | створити групу |
| `PATCH` | `/api/parameter-groups/{id}` | оновити назву групи |
| `DELETE` | `/api/parameter-groups/{id}` | видалити групу |
| `POST` | `/api/parameter-groups/{id}/items` | додати елемент до групи |
| `DELETE` | `/api/parameter-groups/items/{itemId}` | видалити елемент групи |

### Components
| Метод | Шлях | Опис |
|---|---|---|
| `GET` | `/api/components/groups` | список усіх груп компонентів |
| `GET` | `/api/components?groupId={id}` | компоненти за групою |
| `GET` | `/api/components/{id}` | компонент за ID |
| `POST` | `/api/components/groups` | створити групу компонентів |
| `POST` | `/api/components` | створити компонент |
| `PATCH` | `/api/components/{id}` | оновити компонент |
| `DELETE` | `/api/components/{id}` | видалити компонент |

---

## Configuration

| Змінна | За замовчуванням | Опис |
|---|---|---|
| `DB_HOST` | `localhost` | хост PostgreSQL |
| `DB_PORT` | `5432` | порт PostgreSQL |
| `DB_NAME` | `product_db` | назва бази даних |
| `DB_USERNAME` | `postgres` | користувач БД |
| `DB_PASSWORD` | `postgres` | пароль БД |
| `SERVER_PORT` | `8080` | порт сервісу |
| `SEED_ENABLE` | `false` | увімкнути завантаження початкових даних |
| `SEED_MODE` | `init` | режим seed (`init` / `prod`) |
| `SEED_STOP_COUNT` | `1` | пропустити seed якщо в таблиці вже є записи |

---

## Run

```bash
# Запуск PostgreSQL
docker compose up postgres-product -d

# Запуск сервісу
./gradlew :product-service:bootRun
```