Давай проаналізуємо zip файл та зробимо підготовку перед проектуванням api-srvice:
1) перевіримо узгодженість сутностей бази данних та їх полей для нашої задачи та узгодженість сутностей меж сервісами.
   (Якщо є якійсь рекомендаціі, то запишемо це в файл analiz.md)
2) перевіримо контролери та їх параметри, чи достатньо для нашої задачи по високонавантаженю.
   (Якщо є якійсь рекомендаціі, то запишемо це в файл analiz.md)    
3) проаналізуемо json файли, з яких будуть данні в базі данних
    Це тестові данні, щоб перевірити, що проект працює.
   (Потім зформуємо данні для прода)
4) Також може що ще можно оновити в цих сервісах для подальшої роботи
   (Якщо є якійсь рекомендаціі, то запишемо це в файл analiz.md)
----------------------
Скопіюємо усі файли json модулів в відповідну папку docs/architecture/goshop-documentation
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