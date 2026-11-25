package ua.lpnu.kzp;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Форматує результати запитів {@link RepairQueries} для консолі. Сам нічого не обчислює.
 */
public final class StreamReport {

    /** Поріг вартості за замовчуванням, грн. */
    public static final double DEFAULT_MIN_PRICE = 1000.0;

    /** Кількість записів у top-N за замовчуванням. */
    public static final int DEFAULT_TOP = 3;

    /* Забороняє створення екземплярів службового класу. */
    private StreamReport() {
    }

    /**
     * Формує блок «Запити Stream API».
     *
     * @param repairs    коректні записи
     * @param minPrice   поріг для запиту 1
     * @param top        N для запиту 5
     * @param findClient ім'я для пошуку або {@code null}, якщо пошук не потрібен
     * @return текст блоку з платформним роздільником рядків
     */
    public static String format(List<Repair> repairs, double minPrice, int top, String findClient) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.ROOT, "--- Запити Stream API ---%n"));
        sb.append(String.format(Locale.ROOT, "Ремонти від %.2f грн: %d%n",
                minPrice, RepairQueries.expensive(repairs, minPrice).size()));
        sb.append(String.format(Locale.ROOT, "Клієнти: %s%n",
                String.join(", ", RepairQueries.clientNames(repairs))));
        sb.append(String.format(Locale.ROOT, "За типом пристрою: %s%n",
                RepairQueries.countByDeviceKind(repairs).entrySet().stream()
                        .map(e -> e.getKey() + "=" + e.getValue())
                        .collect(Collectors.joining(", "))));
        DoubleSummaryStatistics stats = RepairQueries.hoursStatistics(repairs);
        if (stats.getCount() == 0) {
            sb.append(String.format(Locale.ROOT, "Години: немає даних%n"));
        } else {
            sb.append(String.format(Locale.ROOT, "Години: count=%d, min=%.2f, max=%.2f, avg=%.2f%n",
                    stats.getCount(), stats.getMin(), stats.getMax(), stats.getAverage()));
        }
        sb.append(String.format(Locale.ROOT, "Топ-%d найдорожчих:%n", top));
        RepairQueries.mostExpensive(repairs, top).forEach(r -> sb.append(String.format(Locale.ROOT,
                "  %s, %s, %.2f грн%n", r.client(), r.device(), r.price())));
        sb.append(String.format(Locale.ROOT, "За рівнем і типом: %s%n",
                RepairQueries.countByTierAndKind(repairs)));
        if (findClient != null) {
            Optional<Repair> found = RepairQueries.findByClient(repairs, findClient);
            sb.append(String.format(Locale.ROOT, "Пошук клієнта «%s»: %s%n", findClient,
                    found.map(r -> String.format(Locale.ROOT, "%s, %.2f год, %.2f грн",
                            r.device(), r.hours(), r.price())).orElse("не знайдено")));
        }
        return sb.toString();
    }
}
