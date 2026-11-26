package ua.lpnu.kzp;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Поліморфні показники: обробляє заявки лише через спільний тип {@link RepairOrder},
 * без перевірки конкретного підтипу. Новий підтип не потребує змін у цьому класі.
 */
public final class PolymorphicReport {

    /* Забороняє створення екземплярів службового класу. */
    private PolymorphicReport() {
    }

    /**
     * Рахує заявки за видами, унікальні заявки та суму остаточних вартостей.
     *
     * @param orders заявки різних підтипів
     * @return блок тексту для консолі
     */
    public static String format(List<RepairOrder> orders) {
        // ЛР4: цикл замінено потоками. EnumMap зберігає порядок оголошення констант.
        Map<RepairKind, Long> perKind = orders.stream()
                .collect(Collectors.groupingBy(RepairOrder::kind,
                        () -> new EnumMap<>(RepairKind.class), Collectors.counting()));
        // finalCost() — реалізація фактичного підтипу (динамічне зв'язування).
        double totalFinalCost = orders.stream().mapToDouble(RepairOrder::finalCost).sum();
        // distinct використовує equals/hashCode: логічно рівні заявки рахуються один раз.
        long unique = orders.stream().distinct().count();

        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.ROOT, "--- Поліморфні показники ---%n"));
        for (Map.Entry<RepairKind, Long> entry : perKind.entrySet()) {
            sb.append(String.format(Locale.ROOT, "%-32s %12d%n",
                    "Заявок: " + entry.getKey().label(), entry.getValue()));
        }
        sb.append(String.format(Locale.ROOT, "%-32s %12d%n", "Унікальних заявок", unique));
        sb.append(String.format(Locale.ROOT, "%-32s %12.2f%n", "Остаточна вартість, грн", totalFinalCost));
        return sb.toString();
    }
}
