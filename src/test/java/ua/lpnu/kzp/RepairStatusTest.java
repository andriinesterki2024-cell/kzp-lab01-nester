package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Перевіряє стан заявки та розбір дати й стану з CSV (ЛР4). */
class RepairStatusTest {

    @ParameterizedTest
    @CsvSource({"new,NEW", "IN_PROGRESS,IN_PROGRESS", "Done,DONE", "у роботі,IN_PROGRESS"})
    void parseAcceptsCsvValuesAndLabels(String text, RepairStatus expected) {
        assertEquals(expected, RepairStatus.parse(text));
    }

    @Test
    void openMeansNotDone() {
        assertTrue(RepairStatus.NEW.isOpen());
        assertTrue(RepairStatus.IN_PROGRESS.isOpen());
        assertFalse(RepairStatus.DONE.isOpen());
    }

    @Test
    void unknownStatusIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> RepairStatus.parse("closed"));
        assertTrue(e.getMessage().startsWith("невідомий стан заявки"));
    }

    @Test
    void fromCsvReadsDateAndStatus() {
        RepairOrder order = RepairOrder.fromCsv("Іван;Ноутбук;Ремонт;1.0;650.00;WARRANTY;2026-01-05;in_progress");
        assertInstanceOf(WarrantyRepair.class, order);
        assertEquals(LocalDate.of(2026, 1, 5), order.date());
        assertEquals(RepairStatus.IN_PROGRESS, order.status());
        assertTrue(order.isOpen());
    }

    @Test
    void oldFormatsGetDefaultDateAndDoneStatus() {
        RepairOrder lab1 = RepairOrder.fromCsv("Іван;Ноутбук;Ремонт;1.0;650.00");
        assertEquals(RepairOrder.DEFAULT_DATE, lab1.date());
        assertEquals(RepairStatus.DONE, lab1.status());
    }

    @Test
    void badDateKeepsCause() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> RepairOrder.fromCsv("Іван;Ноутбук;Ремонт;1.0;650.00;PAID;06.01.2026;new"));
        assertEquals("поле date має бути датою YYYY-MM-DD, отримано \"06.01.2026\"", e.getMessage());
        assertTrue(e.getCause() instanceof java.time.format.DateTimeParseException);
    }

    @Test
    void dateAndStatusTakePartInEquality() {
        RepairOrder a = RepairOrder.fromCsv("Іван;Ноутбук;Ремонт;1.0;650.00;PAID;2026-01-05;new");
        RepairOrder b = RepairOrder.fromCsv("Іван;Ноутбук;Ремонт;1.0;650.00;PAID;2026-01-06;new");
        RepairOrder c = RepairOrder.fromCsv("Іван;Ноутбук;Ремонт;1.0;650.00;PAID;2026-01-05;done");
        assertFalse(a.equals(b));
        assertFalse(a.equals(c));
        assertEquals(a, RepairOrder.fromCsv("Іван;Ноутбук;Ремонт;1.0;650.00;PAID;2026-01-05;new"));
    }
}
