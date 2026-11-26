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

    /* Забороняє створення екземплярів службового класу. */
    private StreamReport() {
    }

    /**
     * Формує блок «Запити Stream API».
     *
     * @param orders     коректні заявки
     * @param top        N для запиту 5
     * @param findClient ім'я для пошуку або {@code null}, якщо пошук не потрібен
     * @return текст блоку з платформним роздільником рядків
     */
    public static String format(List<RepairOrder> orders, int top, String findClient) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.ROOT, "--- Запити Stream API ---%n"));
        List<RepairOrder> open = RepairQueries.openOrders(orders);
        sb.append(String.format(Locale.ROOT, "Незавершені заявки: %d%n", open.size()));
        open.forEach(o -> sb.append(String.format(Locale.ROOT, "  %s, %s, %s, %s%n",
                o.client(), o.device(), o.date(), o.status().label())));
        sb.append(String.format(Locale.ROOT, "Пристрої: %s%n", String.join(", ", RepairQueries.devices(orders))));
        sb.append(String.format(Locale.ROOT, "Вартість за видом: %s%n",
                RepairQueries.costByKind(orders).entrySet().stream()
                        .map(e -> String.format(Locale.ROOT, "%s=%.2f", e.getKey().label(), e.getValue()))
                        .collect(Collectors.joining(", "))));
        DoubleSummaryStatistics stats = RepairQueries.costStatistics(orders);
        if (stats.getCount() == 0) {
            // Для порожнього потоку min/max — службові ±Infinity, тому випадок виводиться явно.
            sb.append(String.format(Locale.ROOT, "Вартість: немає даних%n"));
        } else {
            sb.append(String.format(Locale.ROOT, "Вартість: count=%d, min=%.2f, max=%.2f, avg=%.2f, sum=%.2f%n",
                    stats.getCount(), stats.getMin(), stats.getMax(), stats.getAverage(), stats.getSum()));
        }
        sb.append(String.format(Locale.ROOT, "Топ-%d найдорожчих (вартість ↓, дата ↑):%n", top));
        RepairQueries.mostExpensive(orders, top).forEach(o -> sb.append(String.format(Locale.ROOT,
                "  %.2f грн, %s, %s, %s%n", o.price(), o.date(), o.client(), o.device())));
        sb.append(String.format(Locale.ROOT, "За видом і станом: %s%n",
                RepairQueries.countByKindAndStatus(orders).entrySet().stream()
                        .map(e -> e.getKey().label() + "=" + e.getValue().entrySet().stream()
                                .map(s -> s.getKey().label() + ":" + s.getValue())
                                .collect(Collectors.joining(", ", "{", "}")))
                        .collect(Collectors.joining(", "))));
        if (findClient != null) {
            Optional<RepairOrder> found = RepairQueries.findByClient(orders, findClient);
            sb.append(String.format(Locale.ROOT, "Пошук клієнта «%s»: %s%n", findClient,
                    found.map(o -> String.format(Locale.ROOT, "%s, %s, %.2f грн, %s",
                            o.device(), o.date(), o.price(), o.status().label())).orElse("не знайдено")));
        }
        return sb.toString();
    }
}
