package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Перевіряє п'ять запитів Stream API, Optional-пошук і дворівневе групування. */
class RepairQueriesTest {

    private List<RepairOrder> orders;

    @BeforeEach
    void load() throws IOException {
        orders = RepairAnalyzer.parse(FileReport.readLines(Path.of("data", "input-lab04.csv"))).orders();
    }

    private static List<String> clients(List<RepairOrder> list) {
        return list.stream().map(RepairOrder::client).toList();
    }

    @Test
    void filterKeepsOnlyOpenOrdersInSourceOrder() {
        List<RepairOrder> open = RepairQueries.openOrders(orders);
        assertEquals(List.of("Коваль Оксана", "Бондар Марія", "Шевчук Андрій", "Коваль Оксана"), clients(open));
        assertTrue(open.stream().allMatch(RepairOrder::isOpen));
    }

    @Test
    void mapReturnsDistinctDevicesInUkrainianOrder() {
        assertEquals(List.of("Ігрова консоль PS5", "Ноутбук Lenovo", "Планшет iPad", "Пральна машина Bosch",
                "Смартфон Samsung", "Холодильник LG"), RepairQueries.devices(orders));
    }

    @Test
    void groupingSumsCostByKindWithEnumKeys() {
        Map<RepairKind, Double> byKind = RepairQueries.costByKind(orders);
        assertEquals(List.of(RepairKind.WARRANTY, RepairKind.PAID), List.copyOf(byKind.keySet()));
        assertEquals(8150.0, byKind.get(RepairKind.WARRANTY), 1e-9);
        assertEquals(8450.0, byKind.get(RepairKind.PAID), 1e-9);
    }

    @Test
    void statisticsOfCost() {
        DoubleSummaryStatistics stats = RepairQueries.costStatistics(orders);
        assertEquals(7, stats.getCount());
        assertEquals(300.0, stats.getMin(), 1e-9);
        assertEquals(5400.0, stats.getMax(), 1e-9);
        assertEquals(16600.0, stats.getSum(), 1e-9);
    }

    @Test
    void topFiveUsesCostDescendingThenDateAscending() {
        List<RepairOrder> top = RepairQueries.mostExpensive(orders, 5);
        assertEquals(List.of("Шевчук Андрій", "Андрієнко Ірина", "Коваль Оксана", "Бондар Марія", "Лисенко Тарас"),
                clients(top));
        // Однакова вартість 3200: раніша дата стоїть першою.
        assertTrue(top.get(1).date().isBefore(top.get(2).date()));
    }

    @Test
    void topNBoundaries() {
        assertTrue(RepairQueries.mostExpensive(orders, 0).isEmpty());
        assertEquals(orders.size(), RepairQueries.mostExpensive(orders, 100).size());
        assertThrows(IllegalArgumentException.class, () -> RepairQueries.mostExpensive(orders, -1));
    }

    @Test
    void findByClientReturnsFirstOfDuplicates() {
        Optional<RepairOrder> found = RepairQueries.findByClient(orders, "  коваль оксана ");
        assertTrue(found.isPresent());
        assertEquals("Смартфон Samsung", found.get().device());
    }

    @Test
    void findByClientEmptyWhenAbsent() {
        assertEquals(Optional.empty(), RepairQueries.findByClient(orders, "Невідомий Клієнт"));
    }

    @Test
    void twoLevelGroupingByKindAndStatus() {
        Map<RepairKind, Map<RepairStatus, Long>> map = RepairQueries.countByKindAndStatus(orders);
        assertEquals(Map.of(RepairStatus.NEW, 1L, RepairStatus.IN_PROGRESS, 1L), map.get(RepairKind.WARRANTY));
        assertEquals(3L, map.get(RepairKind.PAID).get(RepairStatus.DONE));
    }

    @Test
    void emptyInputGivesEmptyResults() {
        List<RepairOrder> empty = List.of();
        assertTrue(RepairQueries.openOrders(empty).isEmpty());
        assertTrue(RepairQueries.devices(empty).isEmpty());
        assertTrue(RepairQueries.costByKind(empty).isEmpty());
        assertEquals(0, RepairQueries.costStatistics(empty).getCount());
        assertTrue(RepairQueries.mostExpensive(empty, 5).isEmpty());
        assertTrue(RepairQueries.findByClient(empty, "Коваль Оксана").isEmpty());
    }

    @Test
    void duplicatesAreKeptByFilterAndRemovedByDistinct() {
        RepairOrder a = RepairOrder.create("Іван", "Ноутбук", "Ремонт", 1.0, 100.0, RepairKind.PAID,
                LocalDate.of(2026, 1, 1), RepairStatus.NEW);
        List<RepairOrder> twice = List.of(a, a);
        assertEquals(2, RepairQueries.openOrders(twice).size());
        assertEquals(1, RepairQueries.devices(twice).size());
    }

    @Test
    void queriesDoNotModifySource() {
        List<RepairOrder> source = new ArrayList<>(orders);
        List<RepairOrder> copy = List.copyOf(source);
        RepairQueries.mostExpensive(source, 3);
        RepairQueries.openOrders(source);
        RepairQueries.devices(source);
        assertEquals(copy, source);
    }

    @Test
    void resultListsAreUnmodifiable() {
        List<RepairOrder> open = RepairQueries.openOrders(orders);
        assertThrows(UnsupportedOperationException.class, () -> open.add(orders.get(0)));
    }

    @Test
    void nullArgumentsAreRejected() {
        assertThrows(NullPointerException.class, () -> RepairQueries.openOrders(null));
        assertThrows(NullPointerException.class, () -> RepairQueries.findByClient(orders, null));
    }
}
