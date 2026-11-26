package ua.lpnu.kzp;

import java.text.Collator;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;

/**
 * Еталонні циклові реалізації запитів (стан до ЛР4) для перевірки еквівалентності потоків.
 */
final class LoopReference {

    private LoopReference() {
    }

    static List<RepairOrder> openOrders(List<RepairOrder> orders) {
        List<RepairOrder> result = new ArrayList<>();
        for (RepairOrder order : orders) {
            if (order.status() != RepairStatus.DONE) {
                result.add(order);
            }
        }
        return result;
    }

    static List<String> devices(List<RepairOrder> orders) {
        TreeSet<String> set = new TreeSet<>(Collator.getInstance(Locale.of("uk", "UA")));
        for (RepairOrder order : orders) {
            set.add(order.device());
        }
        return new ArrayList<>(set);
    }

    static Map<RepairKind, Double> costByKind(List<RepairOrder> orders) {
        Map<RepairKind, Double> map = new EnumMap<>(RepairKind.class);
        for (RepairOrder order : orders) {
            map.merge(order.kind(), order.price(), Double::sum);
        }
        return map;
    }

    static double[] costStatistics(List<RepairOrder> orders) {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        double sum = 0.0;
        for (RepairOrder order : orders) {
            min = Math.min(min, order.price());
            max = Math.max(max, order.price());
            sum += order.price();
        }
        return new double[] {orders.size(), min, max, sum};
    }

    static List<RepairOrder> mostExpensive(List<RepairOrder> orders, int limit) {
        List<RepairOrder> sorted = new ArrayList<>();
        for (RepairOrder order : orders) {
            int index = 0;
            // Вставка: вартість за спаданням, за рівності — дата за зростанням (стабільно).
            while (index < sorted.size() && precedes(sorted.get(index), order)) {
                index++;
            }
            sorted.add(index, order);
        }
        return sorted.subList(0, Math.min(limit, sorted.size()));
    }

    private static boolean precedes(RepairOrder placed, RepairOrder candidate) {
        if (placed.price() != candidate.price()) {
            return placed.price() > candidate.price();
        }
        return !placed.date().isAfter(candidate.date());
    }

    static Optional<RepairOrder> findByClient(List<RepairOrder> orders, String client) {
        for (RepairOrder order : orders) {
            if (order.client().equalsIgnoreCase(client.strip())) {
                return Optional.of(order);
            }
        }
        return Optional.empty();
    }
}
