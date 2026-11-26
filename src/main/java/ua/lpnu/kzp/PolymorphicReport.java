package ua.lpnu.kzp;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
        // EnumMap зберігає порядок оголошення констант — вивід стабільний.
        Map<RepairKind, Integer> perKind = new EnumMap<>(RepairKind.class);
        double totalFinalCost = 0.0;
        for (RepairOrder order : orders) {
            // Викликається реалізація фактичного підтипу — динамічне зв'язування.
            totalFinalCost += order.finalCost();
            perKind.merge(order.kind(), 1, Integer::sum);
        }
        // HashSet використовує equals/hashCode: логічно рівні заявки рахуються один раз.
        Set<RepairOrder> unique = new HashSet<>(orders);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.ROOT, "--- Поліморфні показники ---%n"));
        for (Map.Entry<RepairKind, Integer> entry : perKind.entrySet()) {
            sb.append(String.format(Locale.ROOT, "%-32s %12d%n",
                    "Заявок: " + entry.getKey().label(), entry.getValue()));
        }
        sb.append(String.format(Locale.ROOT, "%-32s %12d%n", "Унікальних заявок", unique.size()));
        sb.append(String.format(Locale.ROOT, "%-32s %12.2f%n", "Остаточна вартість, грн", totalFinalCost));
        return sb.toString();
    }
}
