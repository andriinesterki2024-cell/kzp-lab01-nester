package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Перевіряє ієрархію RepairOrder: спільний тип, підтипи, поліморфізм, рівність і sealed. */
class RepairHierarchyTest {

    private static WarrantyRepair warranty(double price) {
        return new WarrantyRepair("Коваль Оксана", "Пральна машина Bosch", "Заміна підшипника", 4.5, price, 12);
    }

    private static PaidRepair paid(double price) {
        return new PaidRepair("Коваль Оксана", "Пральна машина Bosch", "Заміна підшипника", 4.5, price, 0.05);
    }

    // --- базовий тип ---

    @Test
    void baseTypeRejectsInvalidCommonStateForEverySubtype() {
        assertThrows(IllegalArgumentException.class,
                () -> new WarrantyRepair(" ", "Ноутбук", "Ремонт", 1.0, 10.0, 12));
        assertThrows(IllegalArgumentException.class,
                () -> new PaidRepair("Іван", "Ноутбук", "Ремонт", 0.0, 10.0, 0.05));
        assertThrows(IllegalArgumentException.class,
                () -> new PaidRepair("Іван", "Ноутбук", "Ремонт", 1.0, -1.0, 0.05));
    }

    @Test
    void subtypeSetsItsKind() {
        assertEquals(RepairKind.WARRANTY, warranty(100).kind());
        assertEquals(RepairKind.PAID, paid(100).kind());
    }

    // --- підтипи: коректні та власні хибні поля ---

    @Test
    void warrantyRepairAcceptsValidFields() {
        WarrantyRepair repair = new WarrantyRepair("Іван", "Ноутбук", "Ремонт", 1.0, 0.0, 36);
        assertEquals(36, repair.warrantyMonths());
        assertEquals(0.0, repair.price(), 0.0);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 37})
    void warrantyRepairRejectsOwnInvalidMonths(int months) {
        assertThrows(IllegalArgumentException.class,
                () -> new WarrantyRepair("Іван", "Ноутбук", "Ремонт", 1.0, 10.0, months));
    }

    @Test
    void paidRepairAcceptsBoundaryDiscounts() {
        assertEquals(0.0, new PaidRepair("Іван", "Ноутбук", "Ремонт", 1.0, 10.0, 0.0).discountRate(), 0.0);
        assertEquals(0.5, new PaidRepair("Іван", "Ноутбук", "Ремонт", 1.0, 10.0, 0.5).discountRate(), 0.0);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.01, 0.51, Double.NaN, Double.POSITIVE_INFINITY})
    void paidRepairRejectsOwnInvalidDiscount(double rate) {
        assertThrows(IllegalArgumentException.class,
                () -> new PaidRepair("Іван", "Ноутбук", "Ремонт", 1.0, 10.0, rate));
    }

    // --- поліморфна операція ---

    @Test
    void subtypesCalculateFinalCostDifferently() {
        RepairOrder w = warranty(3200.0);
        RepairOrder p = paid(3200.0);
        assertEquals(0.0, w.finalCost(), 0.0);
        assertEquals(3040.0, p.finalCost(), 1e-9);
        assertNotEquals(w.finalCost(), p.finalCost());
    }

    @Test
    void paidDiscountStartsExactlyAtThreshold() {
        assertEquals(2999.99, paid(2999.99).finalCost(), 1e-9);
        assertEquals(2850.0, paid(3000.0).finalCost(), 1e-9);
        assertEquals(0.0, paid(0.0).finalCost(), 0.0);
    }

    @Test
    void polymorphicCollectionIsProcessedThroughBaseType() {
        List<RepairOrder> orders = List.of(paid(650.0), warranty(2750.0), paid(3200.0), warranty(5400.0));
        double total = 0.0;
        for (RepairOrder order : orders) {
            total += order.finalCost(); // жодного instanceof: реалізацію обирає JVM
        }
        assertEquals(650.0 + 3040.0, total, 1e-9);
    }

    // --- рівність ---

    @Test
    void equalOrdersHaveSameHashCodeAndOneSetEntry() {
        assertEquals(paid(650.0), paid(650.0));
        assertEquals(paid(650.0).hashCode(), paid(650.0).hashCode());
        Set<RepairOrder> set = new HashSet<>(List.of(paid(650.0), paid(650.0), warranty(650.0)));
        assertEquals(2, set.size());
    }

    @Test
    void differentSubtypesWithSameCommonFieldsAreNotEqual() {
        assertNotEquals(paid(650.0), warranty(650.0));
        assertNotEquals(warranty(650.0), paid(650.0));
    }

    @Test
    void ownFieldTakesPartInEquality() {
        assertNotEquals(new WarrantyRepair("Іван", "Ноутбук", "Ремонт", 1.0, 10.0, 12),
                new WarrantyRepair("Іван", "Ноутбук", "Ремонт", 1.0, 10.0, 24));
        assertNotEquals(new PaidRepair("Іван", "Ноутбук", "Ремонт", 1.0, 10.0, 0.05),
                new PaidRepair("Іван", "Ноутбук", "Ремонт", 1.0, 10.0, 0.10));
    }

    @Test
    void equalOrdersWorkAsHashMapKeys() {
        Map<RepairOrder, String> notes = new HashMap<>();
        notes.put(paid(650.0), "перший");
        notes.put(paid(650.0), "другий");
        assertEquals(1, notes.size());
        assertEquals("другий", notes.get(paid(650.0)));
    }

    // --- фабрика та CSV ---

    @Test
    void factoryCreatesSubtypeByKind() {
        assertInstanceOf(WarrantyRepair.class, RepairOrder.create("І", "П", "Д", 1.0, 1.0, RepairKind.WARRANTY));
        assertInstanceOf(PaidRepair.class, RepairOrder.create("І", "П", "Д", 1.0, 1.0, RepairKind.PAID));
    }

    @Test
    void fromCsvReadsOptionalKindField() {
        assertInstanceOf(WarrantyRepair.class, RepairOrder.fromCsv("Іван;Ноутбук;Ремонт;1.0;650.00;WARRANTY"));
        assertInstanceOf(PaidRepair.class, RepairOrder.fromCsv("Іван;Ноутбук;Ремонт;1.0;650.00;paid"));
        // Формат ЛР1 без шостого поля — платна заявка.
        assertInstanceOf(PaidRepair.class, RepairOrder.fromCsv("Іван;Ноутбук;Ремонт;1.0;650.00"));
    }

    @Test
    void fromCsvRejectsUnknownKindAndTooManyFields() {
        IllegalArgumentException kind = assertThrows(IllegalArgumentException.class,
                () -> RepairOrder.fromCsv("Іван;Ноутбук;Ремонт;1.0;650.00;ОРЕНДА"));
        assertTrue(kind.getMessage().startsWith("невідомий вид ремонту"));
        IllegalArgumentException count = assertThrows(IllegalArgumentException.class,
                () -> RepairOrder.fromCsv("Іван;Ноутбук;Ремонт;1.0;650.00;PAID;2026-01-05;new;зайве"));
        // З ЛР4 формат має ще два необов'язкові поля (дата, стан), тому межа — 8 полів.
        assertEquals("очікується не більше 8 полів, отримано 9", count.getMessage());
    }

    @Test
    void builderUsesFactoryAndKind() {
        RepairOrder built = RepairOrder.builder().client("Іван").device("Ноутбук").diagnosis("Ремонт")
                .hours(1.0).price(10.0).kind(RepairKind.WARRANTY).build();
        assertEquals(new WarrantyRepair("Іван", "Ноутбук", "Ремонт", 1.0, 10.0, 12), built);
    }

    // --- sealed (завдання із зірочкою) ---

    @Test
    void hierarchyIsSealedWithTwoPermittedSubtypes() {
        assertTrue(RepairOrder.class.isSealed());
        assertArrayEquals(new Class<?>[] {WarrantyRepair.class, PaidRepair.class},
                RepairOrder.class.getPermittedSubclasses());
    }

    @Test
    void exhaustiveSwitchDescribesOwnFields() {
        assertEquals("гарантія 12 міс., клієнт не сплачує роботи", RepairDescriber.describe(warranty(10.0)));
        assertEquals("знижка 5% від 3000.00 грн", RepairDescriber.describe(paid(10.0)));
    }
}
