Всі 
----------------------
Скопіюємо усі файли json модулів в відповідну папку docs/architecture/goshop-documentation
------------------------
Файл goshop-seed-long-v1.zip треба проаналізувати, в ньому по проектам є різні json файли.
Треба їх скопіювати в проекти та адаптувати ці json файли та поля сутностей. 
------------------------
Треба в папці docs/architecture/goshop-documentation.
Створити папки с назвами проектів та щоб в кожну
таку папку положити 2 файла з проектів:
README.md та openapi.yaml
Ось така структура повинна получитись:
goshop-documentation/
├── architecture-review.md
├── product-service/
│   ├── README.md
│   └── openapi.yaml
├── compatibility-service/
│   ├── README.md
│   └── openapi.yaml
├── localization-service/
│   ├── README.md
│   └── openapi.yaml
├── pricing-service/
│   ├── README.md
│   └── openapi.yaml
├── recommendations-service/
│   ├── README.md
│   └── openapi.yaml
├── search-service/
│   ├── README.md
│   └── openapi.yaml
└── store-service/
    ├── README.md
    └── openapi.yaml