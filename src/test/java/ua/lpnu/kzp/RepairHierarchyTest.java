package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

/** Перевіряє базовий тип, підтипи, поліморфізм і рівність ієрархії {@link Repair}. */
class RepairHierarchyTest {

    private static final double EPS = 0.0001;

    private static GadgetRepair gadget(double price) {
        return new GadgetRepair("Іван", "Телевізор LG", "Ремонт", 2.0, price, 30);
    }

    private static ApplianceRepair appliance(double price) {
        return new ApplianceRepair("Іван", "Телевізор LG", "Ремонт", 2.0, price, 200.0);
    }

    @Test
    void factoryChoosesSubtypeByDeviceKind() {
        assertInstanceOf(ApplianceRepair.class, Repair.create("Коваль", "Пральна машина Bosch", "Підшипник", 4.5, 2750));
        assertInstanceOf(GadgetRepair.class, Repair.create("Іваненко", "Ноутбук Lenovo", "Термопаста", 1.5, 650));
        // Невідомий пристрій (OTHER) обробляється як ремонт у майстерні — сумісність із ЛР2.
        assertInstanceOf(GadgetRepair.class, Repair.create("Петро", "Електросамокат", "Колесо", 1.0, 400));
    }

    @Test
    void fromCsvUsesFactory() {
        assertInstanceOf(ApplianceRepair.class,
                Repair.fromCsv("Бондар Марія;Холодильник Samsung;Заміна компресора;6.0;5400.00"));
    }

    @Test
    void subtypesCalculateCostDifferentlyForSameInput() {
        Repair g = gadget(1000.0);
        Repair a = appliance(1000.0);
        assertEquals(1000.0, g.totalCost(), EPS);
        assertEquals(1200.0, a.totalCost(), EPS);
        assertNotEquals(g.totalCost(), a.totalCost());
    }

    @Test
    void warrantyAndTierDifferBySubtype() {
        assertEquals(30, gadget(1.0).warrantyDays());
        assertEquals(90, appliance(1.0).warrantyDays());
        assertEquals("Гаджети", gadget(1.0).tierLabel());
        assertEquals("Побутова техніка", appliance(1.0).tierLabel());
    }

    @Test
    void polymorphicProcessingThroughBaseType() {
        List<Repair> repairs = List.of(gadget(650.0), appliance(2750.0), gadget(3200.0));
        double total = 0.0;
        for (Repair r : repairs) {
            total += r.totalCost();
        }
        assertEquals(650.0 + 2950.0 + 3200.0, total, EPS);
    }

    @Test
    void sharedInvariantsAreCheckedInBaseType() {
        assertThrows(IllegalArgumentException.class,
                () -> new GadgetRepair(" ", "Ноутбук", "Ремонт", 1.0, 1.0, 30));
        assertThrows(IllegalArgumentException.class,
                () -> new ApplianceRepair("Іван", "Пральна машина", "Ремонт", 0.0, 1.0, 200.0));
        assertThrows(IllegalArgumentException.class,
                () -> new ApplianceRepair("Іван", "Пральна машина", "Ремонт", 1.0, Double.NaN, 200.0));
    }

    @Test
    void ownFieldsAreValidated() {
        assertThrows(IllegalArgumentException.class, () -> new GadgetRepair("І", "Н", "Р", 1.0, 1.0, -1));
        assertThrows(IllegalArgumentException.class, () -> new GadgetRepair("І", "Н", "Р", 1.0, 1.0, 366));
        assertThrows(IllegalArgumentException.class, () -> new ApplianceRepair("І", "П", "Р", 1.0, 1.0, -0.01));
        assertThrows(IllegalArgumentException.class,
                () -> new ApplianceRepair("І", "П", "Р", 1.0, 1.0, Double.POSITIVE_INFINITY));
    }

    @Test
    void ownFieldBoundariesAreAccepted() {
        assertEquals(0, new GadgetRepair("І", "Н", "Р", 1.0, 1.0, 0).warrantyDays());
        assertEquals(365, new GadgetRepair("І", "Н", "Р", 1.0, 1.0, 365).warrantyDays());
        assertEquals(1.0, new ApplianceRepair("І", "П", "Р", 1.0, 1.0, 0.0).totalCost(), EPS);
    }

    @Test
    void kindIsDerivedFromDevice() {
        assertEquals(DeviceKind.TV, gadget(1.0).kind());
        assertEquals(DeviceKind.WASHER, Repair.create("І", "Пральна машина", "Р", 1.0, 1.0).kind());
    }

    @Test
    void equalsIsReflexiveSymmetricAndHandlesNull() {
        Repair x = gadget(100.0);
        Repair y = gadget(100.0);
        assertEquals(x, x);
        assertEquals(x, y);
        assertEquals(y, x);
        assertFalse(x.equals(null));
        assertFalse(x.equals("рядок"));
    }

    @Test
    void equalObjectsHaveSameHashCode() {
        assertEquals(gadget(100.0).hashCode(), gadget(100.0).hashCode());
        assertEquals(appliance(5.0).hashCode(), appliance(5.0).hashCode());
    }

    @Test
    void differentSubtypesWithSameDataAreNotEqual() {
        assertNotEquals(gadget(100.0), appliance(100.0));
        assertNotEquals(appliance(100.0), gadget(100.0));
    }

    @Test
    void ownFieldTakesPartInEquality() {
        assertNotEquals(new GadgetRepair("І", "Н", "Р", 1.0, 1.0, 30), new GadgetRepair("І", "Н", "Р", 1.0, 1.0, 60));
        assertNotEquals(new ApplianceRepair("І", "П", "Р", 1.0, 1.0, 200), new ApplianceRepair("І", "П", "Р", 1.0, 1.0, 0));
    }

    @Test
    void hashSetDoesNotDuplicateEqualRepairs() {
        Set<Repair> set = new HashSet<>(List.of(gadget(100.0), gadget(100.0), appliance(100.0), appliance(100.0)));
        assertEquals(2, set.size());
        assertTrue(set.contains(gadget(100.0)));
    }

    @Test
    void hashMapFindsValueByLogicallyEqualKey() {
        Map<Repair, String> map = new HashMap<>();
        map.put(appliance(2750.0), "квитанція 7");
        assertEquals("квитанція 7", map.get(appliance(2750.0)));
        assertEquals(null, map.get(gadget(2750.0)));
    }

    @Test
    void describerCoversEverySubtype() {
        assertEquals("ремонт у майстерні, гарантія 30 днів", RepairDescriber.describe(gadget(1.0)));
        assertEquals("виїзд майстра 200.00 грн, гарантія 90 днів", RepairDescriber.describe(appliance(1.0)));
    }

    @Test
    void polymorphicReportCountsTiersUniqueAndCost() {
        String text = PolymorphicReport.format(List.of(gadget(100.0), gadget(100.0), appliance(300.0)));
        assertTrue(text.contains("Гаджети"));
        assertTrue(text.matches("(?s).*Гаджети\\s+2.*"));
        assertTrue(text.matches("(?s).*Побутова техніка\\s+1.*"));
        assertTrue(text.matches("(?s).*Унікальних ремонтів\\s+2.*"));
        assertTrue(text.contains("700.00"));
    }

    @Test
    void polymorphicReportForEmptyList() {
        String text = PolymorphicReport.format(List.of());
        assertTrue(text.matches("(?s).*Унікальних ремонтів\\s+0.*"));
        assertTrue(text.contains("0.00"));
    }
}
