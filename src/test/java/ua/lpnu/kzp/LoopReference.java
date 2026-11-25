package ua.lpnu.kzp;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Еталонні циклові реалізації запитів (стиль ЛР1–3) для перевірки, що потоки дають той самий результат.
 */
final class LoopReference {

    private LoopReference() {
    }

    static List<Repair> expensive(List<Repair> repairs, double minPrice) {
        List<Repair> result = new ArrayList<>();
        for (Repair r : repairs) {
            if (r.price() >= minPrice) {
                result.add(r);
            }
        }
        return result;
    }

    static List<String> clientNames(List<Repair> repairs) {
        TreeSet<String> set = new TreeSet<>(RepairQueries.ukrainianOrder());
        for (Repair r : repairs) {
            set.add(r.client());
        }
        return new ArrayList<>(set);
    }

    static Map<DeviceKind, Long> countByDeviceKind(List<Repair> repairs) {
        Map<DeviceKind, Long> map = new EnumMap<>(DeviceKind.class);
        for (Repair r : repairs) {
            map.merge(r.kind(), 1L, Long::sum);
        }
        return map;
    }

    static double[] hoursMinMaxSumCount(List<Repair> repairs) {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        double sum = 0.0;
        for (Repair r : repairs) {
            min = Math.min(min, r.hours());
            max = Math.max(max, r.hours());
            sum += r.hours();
        }
        return new double[] {min, max, sum, repairs.size()};
    }

    static List<Repair> mostExpensive(List<Repair> repairs, int limit) {
        // Сортування вставками за тими самими критеріями, далі перші limit елементів.
        List<Repair> sorted = new ArrayList<>();
        for (Repair r : repairs) {
            int i = 0;
            while (i < sorted.size() && RepairQueries.mostExpensiveFirst().compare(sorted.get(i), r) <= 0) {
                i++;
            }
            sorted.add(i, r);
        }
        return new ArrayList<>(sorted.subList(0, Math.min(limit, sorted.size())));
    }

    static Repair findByClient(List<Repair> repairs, String client) {
        for (Repair r : repairs) {
            if (r.client().equalsIgnoreCase(client.strip())) {
                return r;
            }
        }
        return null;
    }
}
