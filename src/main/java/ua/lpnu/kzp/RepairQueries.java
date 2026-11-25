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
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Запити Stream API над колекцією ремонтів (лабораторна робота № 4).
 *
 * <p>Кожен метод створює власний потік з {@code repairs.stream()}, не змінює вхідну
 * колекцію, не форматує текст і не перевіряє конкретний підтип {@link Repair}:
 * різниця підтипів доступна лише через поліморфні методи. Форматує результати {@link Main}.</p>
 */
public final class RepairQueries {

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
     * Запит 1 (filter): ремонти з вартістю не меншою за поріг.
     *
     * @param repairs  колекція ремонтів
     * @param minPrice поріг вартості, грн (включно)
     * @return ремонти в порядку колекції
     */
    public static List<Repair> expensive(Collection<Repair> repairs, double minPrice) {
        Objects.requireNonNull(repairs, "repairs");
        return repairs.stream()
                .filter(repair -> repair.price() >= minPrice)
                .toList();
    }

    /**
     * Запит 2 (map): імена клієнтів без повторів за алфавітом.
     *
     * @param repairs колекція ремонтів
     * @return список клієнтів за українським алфавітом
     */
    public static List<String> clientNames(Collection<Repair> repairs) {
        Objects.requireNonNull(repairs, "repairs");
        return repairs.stream()
                .map(Repair::client)
                .distinct()
                .sorted(ukrainianOrder())
                .toList();
    }

    /**
     * Запит 3 (groupingBy): кількість ремонтів за типом пристрою.
     *
     * @param repairs колекція ремонтів
     * @return карта з ключами в порядку оголошення {@link DeviceKind}; типи без ремонтів відсутні
     */
    public static Map<DeviceKind, Long> countByDeviceKind(Collection<Repair> repairs) {
        Objects.requireNonNull(repairs, "repairs");
        return repairs.stream()
                .collect(Collectors.groupingBy(Repair::kind,
                        () -> new EnumMap<>(DeviceKind.class), Collectors.counting()));
    }

    /**
     * Запит 4 (summarizingDouble): статистика тривалості ремонтів.
     *
     * @param repairs колекція ремонтів
     * @return кількість, мінімум, максимум, сума й середнє годин; для порожньої колекції count = 0
     */
    public static DoubleSummaryStatistics hoursStatistics(Collection<Repair> repairs) {
        Objects.requireNonNull(repairs, "repairs");
        return repairs.stream()
                .collect(Collectors.summarizingDouble(Repair::hours));
    }

    /**
     * Складений компаратор: вартість за спаданням, за рівної вартості — клієнт за алфавітом.
     * {@code reversed()} застосовано лише до першого ключа, до {@code thenComparing}.
     *
     * @return компаратор для top-N
     */
    public static Comparator<Repair> mostExpensiveFirst() {
        return Comparator.comparingDouble(Repair::price)
                .reversed()
                .thenComparing(Repair::client, ukrainianOrder());
    }

    /**
     * Запит 5 (sorted + limit): N найдорожчих ремонтів.
     *
     * @param repairs колекція ремонтів
     * @param limit   кількість, не від'ємна
     * @return щонайбільше {@code limit} записів у порядку {@link #mostExpensiveFirst()}
     * @throws IllegalArgumentException якщо {@code limit < 0}
     */
    public static List<Repair> mostExpensive(Collection<Repair> repairs, int limit) {
        Objects.requireNonNull(repairs, "repairs");
        if (limit < 0) {
            throw new IllegalArgumentException("limit не може бути від'ємним");
        }
        return repairs.stream()
                .sorted(mostExpensiveFirst())
                .limit(limit)
                .toList();
    }

    /**
     * Пошук (Optional): перший ремонт клієнта без урахування регістру й пробілів.
     *
     * @param repairs колекція ремонтів
     * @param client  ім'я клієнта
     * @return знайдений ремонт або {@link Optional#empty()}; ніколи не {@code null}
     */
    public static Optional<Repair> findByClient(Collection<Repair> repairs, String client) {
        Objects.requireNonNull(repairs, "repairs");
        Objects.requireNonNull(client, "client");
        String wanted = client.strip();
        return repairs.stream()
                .filter(repair -> repair.client().equalsIgnoreCase(wanted))
                .findFirst();
    }

    /**
     * Завдання із зірочкою: дворівневе групування «рівень обслуговування → тип пристрою → кількість».
     * Назву рівня повертає поліморфний {@link Repair#tierLabel()}, тому перевірки підтипу немає.
     *
     * @param repairs колекція ремонтів
     * @return вкладена карта з відтворюваним порядком ключів
     */
    public static Map<String, Map<DeviceKind, Long>> countByTierAndKind(Collection<Repair> repairs) {
        Objects.requireNonNull(repairs, "repairs");
        return repairs.stream()
                .collect(Collectors.groupingBy(Repair::tierLabel, TreeMap::new,
                        Collectors.groupingBy(Repair::kind,
                                () -> new EnumMap<>(DeviceKind.class), Collectors.counting())));
    }
}
