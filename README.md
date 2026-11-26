# Ремонтна майстерня — лабораторні роботи № 1–4 (КЗП, варіант 16)

Автор: Нестер А.В., група КІ-303.

## Призначення

Консольна Java-програма читає записи про ремонти з `data/input.csv`, перевіряє
кожен рядок, пропускає хибні з указанням номера рядка та причини, обчислює
показники та виводить один і той самий звіт у консоль і в файл.

## Формат входу

Файл у кодуванні UTF-8, один запис у рядку, поля розділено `;`, десятковий
роздільник — крапка:

```text
client;device;diagnosis;hours;price
Іваненко Петро;Ноутбук Lenovo IdeaPad;Заміна термопасти;1.5;650.00
```

| Поле | Тип | Правило |
|------|-----|---------|
| client | String | непорожнє |
| device | String | непорожнє |
| diagnosis | String | непорожнє |
| hours | double | > 0, не NaN/Infinity |
| price | double | ≥ 0, не NaN/Infinity |

## Показники

1. Кількість коректних записів.
2. Сумарний час ремонтів, год.
3. Загальний виторг, грн.
4. Найдовший ремонт (год, клієнт і пристрій).

## Збірка та запуск

```text
mvnw.cmd clean verify          # Windows
./mvnw clean verify            # macOS / Ubuntu
java -jar target/lab01-4.0.0.jar --input data/input.csv --output out/report.txt
java -jar target/lab01-4.0.0.jar --help
java -jar target/lab01-4.0.0.jar --version
```

## Приклад

```text
> java -jar target/lab01-4.0.0.jar
=== Звіт ремонтної майстерні ===
Показник                             Значення
Кількість коректних записів                 6
Сумарний час ремонтів, год              11.50
Загальний виторг, грн                 8800.00
Найдовший ремонт, год                    4.50
  (клієнт: Бондар Марія; пристрій: Пральна машина Bosch)
Пропущено рядків: 5
  Рядок 6: порожній рядок
  Рядок 7: порожнє поле diagnosis (діагноз)
  Рядок 8: поле hours має нечислове значення "два"
  Рядок 9: price не може бути від'ємним
  Рядок 10: очікується 5 полів, отримано 4
Звіт збережено у файл: out\report.txt
```

Якщо у Windows-консолі кирилиця відображається некоректно, виконайте `chcp 65001`.

Коди завершення: `0` — успіх, `1` — помилка аргументів або файла, `2` — немає
жодного коректного запису.

## Структура (версія 2.0.0, лабораторна робота № 2)

```text
src/main/java/ua/lpnu/kzp/
├── Main.java            CLI-аргументи та оркестрація
├── FileReport.java      читання/запис файлів у UTF-8 через Path
├── RepairOrder.java     клас-сутність: private final поля, інваріанти, fromCsv, Builder
├── HoursPrice.java      record (hours, price): єдине місце числових правил
├── RepairAnalyzer.java  List<String> → List<RepairOrder> + помилки → показники
├── RepairSummary.java   record з показниками звіту
└── ReportFormatter.java форматування звіту (Locale.ROOT, %n)
baseline/                контрольні вхід і звіт лабораторної роботи № 1
docs/REPORT-lab01.md     звіт лабораторної роботи № 1
```

### Клас `RepairOrder`

Назви класу-сутності й record узято з таблиці варіантів ЛР2 (варіант 16: `RepairOrder`, `HoursPrice`).


- Поля `client`, `device`, `diagnosis`, `hours`, `price` — `private final`, сеттерів немає.
- Конструктор перевіряє інваріанти: текстові поля не `null` і не порожні (обрізаються),
  `hours` скінченне і `> 0`, `price` скінченна і `>= 0` (через record `HoursPrice`).
- `RepairOrder.fromCsv(line)` — розбір одного рядка; порядок перевірок і тексти помилок
  збігаються з лабораторною роботою № 1.
- `RepairOrder.builder()...build()` — будівник, який викликає той самий конструктор.
- `toString()` форматує числа з двома знаками через `Locale.ROOT`.

### Відмінності від лабораторної роботи № 1

| Було (1.0.0) | Стало (2.0.0) |
|--------------|---------------|
| запис — `String[]` з індексами 0–4 | об'єкт `RepairOrder` з іменованими методами |
| перевірка в `RepairAnalyzer.parseLine` | перевірка в конструкторі `RepairOrder` і record `HoursPrice` |
| показники рахуються в циклі розбору | `analyze` збирає `List<RepairOrder>`, `summarize` рахує показники |
| 14 тестів | 38 тестів (старі 14 + 24 нові), тест сумісності з baseline |

Формат CSV, показники й текст звіту не змінилися — це перевіряє
`BaselineCompatibilityTest` та `Compare-Object` з `baseline/report-lab01.txt`.

## Ієрархія заявок (версія 3.0.0, лабораторна робота № 3)

За таблицею варіантів ЛР3 (варіант 16): базовий тип `RepairOrder`, підтипи `WarrantyRepair` і `PaidRepair`,
enum `RepairKind`, поліморфна операція — остаточна вартість `finalCost()`.

| Тип | Власне поле | Остаточна вартість |
|-----|-------------|--------------------|
| `WarrantyRepair` | `warrantyMonths` (1…36, типово 12) | 0.00 — роботи покриває гарантія |
| `PaidRepair` | `discountRate` (0…0.5, типово 0.05) | `price`; від 3000 грн — зі знижкою |

- `RepairOrder` — `abstract sealed`, спільні поля й інваріанти; підтип обирає лише фабрика `create(..., kind)`.
- Формат CSV: `client;device;diagnosis;hours;price[;kind]`, `kind` — `WARRANTY` або `PAID`
  (рядок формату ЛР1 без виду стає платною заявкою, тому звіт ЛР1 не змінився).
- Рівність: `getClass()` + спільні поля + власне поле; перевірено в `HashSet` і `HashMap`.
- Обрано спадкування (гарантійний ремонт *є* заявкою) і композицію для `HoursPrice` (заявка *має* години й ціну).
- Поліморфний блок (`PolymorphicReport`) друкується лише в консоль; ★ `RepairDescriber` — вичерпний `switch` за sealed-ієрархією.

```text
java -jar target/lab01-4.0.0.jar --input data/input-lab03.csv --output out/report-lab03.txt
```

## Запити Stream API (версія 4.0.0, лабораторна робота № 4)

За таблицею варіантів ЛР4 (варіант 16) заявка має клієнта, пристрій, несправність, **дату**, вартість і вид;
додано також **стан** (`RepairStatus`: `new`, `in_progress`, `done`) для фільтра «заявка незавершена».
Формат CSV: `client;device;diagnosis;hours;price[;kind[;date[;status]]]`; рядки ЛР1–3 отримують
вид PAID, дату 2026-01-01 і стан DONE, тому звіт ЛР1 не змінився.

| № | Метод `RepairQueries` | Операції | Результат |
|---|------------------------|----------|-----------|
| 1 | `openOrders` | filter(isOpen) → toList | `List<RepairOrder>` |
| 2 | `devices` | map(device) → distinct → sorted(Collator uk) → toList | `List<String>` |
| 3 | `costByKind` | groupingBy(kind, EnumMap, summingDouble(price)) | `Map<RepairKind, Double>` |
| 4 | `costStatistics` | summarizingDouble(price) | `DoubleSummaryStatistics` |
| 5 | `mostExpensive(orders, N)` | sorted(вартість ↓, дата ↑) → limit(N), N = 5 | `List<RepairOrder>` |
| — | `findByClient` | filter → findFirst | `Optional<RepairOrder>` |
| ★ | `countByKindAndStatus` | groupingBy(kind, groupingBy(status, counting())) | вкладена карта |

```text
java -jar target/lab01-4.0.0.jar --input data/input-lab04.csv --top 5 --find-client "Коваль Оксана"
```
