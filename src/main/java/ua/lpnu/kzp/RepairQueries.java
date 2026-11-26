package ua.lpnu.kzp;

import java.text.Collator;
import java.util.Collection;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Запити Stream API над колекцією заявок (лабораторна робота № 4, варіант 16 за таблицею:
 * фільтр — заявка незавершена; map — пристрої; групування — сума вартості за видом заявки;
 * статистика — вартість; top-5 — вартість спаданням і дата; Optional — пошук за клієнтом).
 *
 * <p>Кожен метод створює власний потік з {@code orders.stream()}, не змінює вхідну колекцію,
 * не форматує текст і не перевіряє конкретний підтип {@link RepairOrder}. Форматує результати
 * {@link StreamReport}.</p>
 */
public final class RepairQueries {

    /** N у запиті top-N за домовленістю методичних вказівок. */
    public static final int DEFAULT_TOP = 5;

    /* Забороняє створення екземплярів службового класу. */
    private RepairQueries() {
    }

    /**
     * Порівняння рядків за правилами українського алфавіту. Природний порядок String
     * порівнює коди Unicode, і «І» (U+0406) опинилася б перед «А» (U+0410).
     *
     * @return новий колатор для української мови
     */
    public static Comparator<Object> ukrainianOrder() {
        return Collator.getInstance(Locale.of("uk", "UA"));
    }

    /**
     * Запит 1 (filter): незавершені заявки (стан «нова» або «у роботі»).
     *
     * @param orders колекція заявок
     * @return незавершені заявки в порядку колекції
     */
    public static List<RepairOrder> openOrders(Collection<RepairOrder> orders) {
        Objects.requireNonNull(orders, "orders");
        return orders.stream()
                .filter(RepairOrder::isOpen)
                .toList();
    }

    /**
     * Запит 2 (map): пристрої без повторів за українським алфавітом.
     *
     * @param orders колекція заявок
     * @return назви пристроїв
     */
    public static List<String> devices(Collection<RepairOrder> orders) {
        Objects.requireNonNull(orders, "orders");
        return orders.stream()
                .map(RepairOrder::device)
                .distinct()
                .sorted(ukrainianOrder())
                .toList();
    }

    /**
     * Запит 3 (groupingBy): сума вартості робіт за видом заявки.
     *
     * @param orders колекція заявок
     * @return карта з ключами в порядку оголошення {@link RepairKind}; види без заявок відсутні
     */
    public static Map<RepairKind, Double> costByKind(Collection<RepairOrder> orders) {
        Objects.requireNonNull(orders, "orders");
        return orders.stream()
                .collect(Collectors.groupingBy(RepairOrder::kind,
                        () -> new EnumMap<>(RepairKind.class),
                        Collectors.summingDouble(RepairOrder::price)));
    }

    /**
     * Запит 4 (summarizingDouble): статистика вартості робіт.
     *
     * @param orders колекція заявок
     * @return кількість, мінімум, максимум, сума й середнє; для порожньої колекції count = 0
     */
    public static DoubleSummaryStatistics costStatistics(Collection<RepairOrder> orders) {
        Objects.requireNonNull(orders, "orders");
        return orders.stream()
                .collect(Collectors.summarizingDouble(RepairOrder::price));
    }

    /**
     * Складений компаратор top-N: вартість за спаданням, за рівної вартості — дата за зростанням.
     * {@code reversed()} застосовано лише до першого ключа, до {@code thenComparing}.
     *
     * @return компаратор для top-N
     */
    public static Comparator<RepairOrder> mostExpensiveFirst() {
        return Comparator.comparingDouble(RepairOrder::price)
                .reversed()
                .thenComparing(RepairOrder::date);
    }

    /**
     * Запит 5 (sorted + limit): N найдорожчих заявок.
     *
     * @param orders колекція заявок
     * @param limit  кількість; 0 — порожній результат, більше за розмір — усі заявки
     * @return щонайбільше {@code limit} заявок у порядку {@link #mostExpensiveFirst()}
     * @throws IllegalArgumentException якщо {@code limit < 0}
     */
    public static List<RepairOrder> mostExpensive(Collection<RepairOrder> orders, int limit) {
        Objects.requireNonNull(orders, "orders");
        if (limit < 0) {
            throw new IllegalArgumentException("limit не може бути від'ємним");
        }
        return orders.stream()
                .sorted(mostExpensiveFirst())
                .limit(limit)
                .toList();
    }

    /**
     * Пошук (Optional): перша заявка клієнта у початковому порядку, без урахування регістру й пробілів.
     *
     * @param orders колекція заявок
     * @param client ім'я клієнта
     * @return знайдена заявка або {@link Optional#empty()}; ніколи не {@code null}
     */
    public static Optional<RepairOrder> findByClient(Collection<RepairOrder> orders, String client) {
        Objects.requireNonNull(orders, "orders");
        Objects.requireNonNull(client, "client");
        String wanted = client.strip();
        return orders.stream()
                .filter(order -> order.client().equalsIgnoreCase(wanted))
                .findFirst();
    }

    /**
     * Завдання із зірочкою: дворівневе групування «вид заявки → стан → кількість».
     *
     * @param orders колекція заявок
     * @return вкладена карта з порядком ключів за оголошенням enum
     */
    public static Map<RepairKind, Map<RepairStatus, Long>> countByKindAndStatus(Collection<RepairOrder> orders) {
        Objects.requireNonNull(orders, "orders");
        return orders.stream()
                .collect(Collectors.groupingBy(RepairOrder::kind, () -> new EnumMap<>(RepairKind.class),
                        Collectors.groupingBy(RepairOrder::status,
                                () -> new EnumMap<>(RepairStatus.class), Collectors.counting())));
    }
}
