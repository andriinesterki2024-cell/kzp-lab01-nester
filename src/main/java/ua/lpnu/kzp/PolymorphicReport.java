package ua.lpnu.kzp;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Поліморфні показники: обробляє записи лише через спільний тип {@link Repair},
 * без перевірки конкретного підтипу. Новий підтип не потребує змін у цьому класі.
 */
public final class PolymorphicReport {

    /* Забороняє створення екземплярів службового класу. */
    private PolymorphicReport() {
    }

    /**
     * Рахує записи за рівнями, унікальні записи та вартість за правилами підтипів.
     *
     * @param repairs записи різних підтипів
     * @return блок тексту для консолі
     */
    public static String format(List<Repair> repairs) {
        // Викликається реалізація фактичного підтипу — динамічне зв'язування.
        Map<String, Long> perTier = repairs.stream()
                .collect(Collectors.groupingBy(Repair::tierLabel, TreeMap::new, Collectors.counting()));
        double totalCost = repairs.stream().mapToDouble(Repair::totalCost).sum();
        // distinct використовує equals/hashCode: однакові записи рахуються один раз.
        long unique = repairs.stream().distinct().count();

        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.ROOT, "--- Поліморфні показники ---%n"));
        for (Map.Entry<String, Long> entry : perTier.entrySet()) {
            sb.append(String.format(Locale.ROOT, "%-32s %12d%n", entry.getKey(), entry.getValue()));
        }
        sb.append(String.format(Locale.ROOT, "%-32s %12d%n", "Унікальних ремонтів", unique));
        sb.append(String.format(Locale.ROOT, "%-32s %12.2f%n", "Вартість за правилами, грн", totalCost));
        return sb.toString();
    }
}
