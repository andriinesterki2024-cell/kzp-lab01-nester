package ua.lpnu.kzp;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
        Map<String, Integer> perTier = new TreeMap<>();
        double totalCost = 0.0;
        for (Repair repair : repairs) {
            // Викликається реалізація фактичного підтипу — динамічне зв'язування.
            totalCost += repair.totalCost();
            perTier.merge(repair.tierLabel(), 1, Integer::sum);
        }
        // HashSet використовує equals/hashCode: однакові записи рахуються один раз.
        Set<Repair> unique = new HashSet<>(repairs);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.ROOT, "--- Поліморфні показники ---%n"));
        for (Map.Entry<String, Integer> entry : perTier.entrySet()) {
            sb.append(String.format(Locale.ROOT, "%-32s %12d%n", entry.getKey(), entry.getValue()));
        }
        sb.append(String.format(Locale.ROOT, "%-32s %12d%n", "Унікальних ремонтів", unique.size()));
        sb.append(String.format(Locale.ROOT, "%-32s %12.2f%n", "Вартість за правилами, грн", totalCost));
        return sb.toString();
    }
}
