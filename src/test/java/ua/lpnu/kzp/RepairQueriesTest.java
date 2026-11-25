package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Перевіряє п'ять запитів Stream API, Optional-пошук і дворівневе групування. */
class RepairQueriesTest {

    private static final double EPS = 0.0001;
    private static List<Repair> data;

    @BeforeAll
    static void load() throws IOException {
        data = RepairAnalyzer.parse(FileReport.readLines(Path.of("data", "input-lab04.csv"))).repairs();
    }

    private static List<String> clients(List<Repair> repairs) {
        return repairs.stream().map(Repair::client).toList();
    }

    @Test
    void expensiveIncludesBoundaryPrice() {
        List<Repair> result = RepairQueries.expensive(data, 1000.0);
        assertEquals(5, result.size());
        assertTrue(clients(result).contains("Шевчук Андрій"));
    }

    @Test
    void expensiveKeepsCollectionOrder() {
        assertEquals(List.of("Коваль Оксана", "Бондар Марія", "Андрієнко Ірина", "Шевчук Андрій", "Лисенко Тарас"),
                clients(RepairQueries.expensive(data, 1000.0)));
    }

    @Test
    void expensiveWithHighThresholdIsEmpty() {
        assertTrue(RepairQueries.expensive(data, 1_000_000.0).isEmpty());
    }

    @Test
    void clientNamesAreDistinctAndInUkrainianOrder() {
        assertEquals(List.of("Андрієнко Ірина", "Бондар Марія", "Іваненко Петро", "Коваль Оксана",
                "Лисенко Тарас", "Шевчук Андрій"), RepairQueries.clientNames(data));
    }

    @Test
    void countByDeviceKindHasTypedKeysInEnumOrder() {
        Map<DeviceKind, Long> counts = RepairQueries.countByDeviceKind(data);
        assertEquals(List.of(DeviceKind.LAPTOP, DeviceKind.PHONE, DeviceKind.TABLET, DeviceKind.PRINTER,
                DeviceKind.CONSOLE, DeviceKind.TV, DeviceKind.WASHER), new ArrayList<>(counts.keySet()));
        assertEquals(1L, counts.get(DeviceKind.WASHER));
        assertEquals(null, counts.get(DeviceKind.FRIDGE));
    }

    @Test
    void hoursStatisticsValues() {
        DoubleSummaryStatistics stats = RepairQueries.hoursStatistics(data);
        assertEquals(7, stats.getCount());
        assertEquals(0.5, stats.getMin(), EPS);
        assertEquals(4.5, stats.getMax(), EPS);
        assertEquals(14.25, stats.getSum(), EPS);
        assertEquals(14.25 / 7, stats.getAverage(), EPS);
    }

    @Test
    void mostExpensiveUsesSecondaryCriterionOnTie() {
        List<Repair> top = RepairQueries.mostExpensive(data, 3);
        // Дві ціни по 3200: другий критерій — клієнт за алфавітом.
        assertEquals(List.of("Андрієнко Ірина", "Коваль Оксана", "Бондар Марія"), clients(top));
    }

    @Test
    void mostExpensiveLimitBoundaries() {
        assertTrue(RepairQueries.mostExpensive(data, 0).isEmpty());
        assertEquals(7, RepairQueries.mostExpensive(data, 7).size());
        assertEquals(7, RepairQueries.mostExpensive(data, 100).size());
        assertThrows(IllegalArgumentException.class, () -> RepairQueries.mostExpensive(data, -1));
    }

    @Test
    void reversedAppliesOnlyToFirstKey() {
        Repair a = Repair.create("Б", "Ноутбук", "Р", 1.0, 100.0);
        Repair b = Repair.create("А", "Ноутбук", "Р", 1.0, 100.0);
        assertTrue(RepairQueries.mostExpensiveFirst().compare(b, a) < 0);
    }

    @Test
    void findByClientReturnsFirstMatchIgnoringCase() {
        Optional<Repair> found = RepairQueries.findByClient(data, "  коваль оксана ");
        assertTrue(found.isPresent());
        assertEquals(DeviceKind.PHONE, found.get().kind());
    }

    @Test
    void findByClientMissingIsEmpty() {
        assertEquals(Optional.empty(), RepairQueries.findByClient(data, "Невідомий"));
    }

    @Test
    void twoLevelGroupingByTierAndKind() {
        Map<String, Map<DeviceKind, Long>> grouped = RepairQueries.countByTierAndKind(data);
        assertEquals(List.of("Гаджети", "Побутова техніка"), new ArrayList<>(grouped.keySet()));
        assertEquals(5, grouped.get("Гаджети").values().stream().mapToLong(Long::longValue).sum());
        assertEquals(1L, grouped.get("Побутова техніка").get(DeviceKind.TV));
    }

    @Test
    void emptyInputForEveryQuery() {
        List<Repair> empty = List.of();
        assertTrue(RepairQueries.expensive(empty, 0).isEmpty());
        assertTrue(RepairQueries.clientNames(empty).isEmpty());
        assertTrue(RepairQueries.countByDeviceKind(empty).isEmpty());
        assertEquals(0, RepairQueries.hoursStatistics(empty).getCount());
        assertEquals(0.0, RepairQueries.hoursStatistics(empty).getAverage(), EPS);
        assertTrue(RepairQueries.mostExpensive(empty, 3).isEmpty());
        assertEquals(Optional.empty(), RepairQueries.findByClient(empty, "Коваль Оксана"));
        assertTrue(RepairQueries.countByTierAndKind(empty).isEmpty());
    }

    @Test
    void duplicatesAreHandled() {
        Repair r = Repair.create("Іван", "Ноутбук", "Ремонт", 1.0, 100.0);
        List<Repair> dup = List.of(r, Repair.create("Іван", "Ноутбук", "Ремонт", 1.0, 100.0));
        assertEquals(List.of("Іван"), RepairQueries.clientNames(dup));
        assertEquals(2L, RepairQueries.countByDeviceKind(dup).get(DeviceKind.LAPTOP));
        assertSame(r, RepairQueries.findByClient(dup, "Іван").orElseThrow());
    }

    @Test
    void sourceCollectionIsNotModified() {
        List<Repair> copy = new ArrayList<>(data);
        RepairQueries.expensive(copy, 0);
        RepairQueries.clientNames(copy);
        RepairQueries.countByDeviceKind(copy);
        RepairQueries.hoursStatistics(copy);
        RepairQueries.mostExpensive(copy, 3);
        RepairQueries.findByClient(copy, "x");
        RepairQueries.countByTierAndKind(copy);
        assertEquals(data, copy);
    }

    @Test
    void nullArgumentsAreRejected() {
        assertThrows(NullPointerException.class, () -> RepairQueries.expensive(null, 0));
        assertThrows(NullPointerException.class, () -> RepairQueries.findByClient(data, null));
    }
}
