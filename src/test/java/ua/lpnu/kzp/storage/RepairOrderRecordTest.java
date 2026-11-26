package ua.lpnu.kzp.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.junit.jupiter.api.Test;
import ua.lpnu.kzp.RepairKind;
import ua.lpnu.kzp.RepairOrder;
import ua.lpnu.kzp.RepairStatus;

/** Перевіряє запис варіанта: інваріанти, рівність, ключ customer + date, розбір полів і причини помилок. */
class RepairOrderRecordTest {

    private static final LocalDate DAY = LocalDate.of(2026, 1, 5);

    private static RepairOrderRecord sample() {
        return new RepairOrderRecord("Іваненко Петро", "Ноутбук Lenovo", "Заміна термопасти", DAY, 650.0, RepairKind.PAID);
    }

    /* Поля в порядку стовпців: cost, customer, date, device, fault, kind. */
    private static List<String> fields(String cost, String date, String kind) {
        return List.of(cost, "Іваненко Петро", date, "Ноутбук Lenovo", "Заміна термопасти", kind);
    }

    @Test
    void fromOrderCopiesFieldsOfAnySubtype() {
        RepairOrder paid = RepairOrder.create("Іваненко Петро", "Ноутбук Lenovo", "Заміна термопасти", 1.5, 650.0,
                RepairKind.PAID, DAY, RepairStatus.DONE);
        assertEquals(sample(), RepairOrderRecord.fromOrder(paid));
        RepairOrder warranty = RepairOrder.create("Бондар Марія", "Пральна машина", "Підшипник", 4.5, 2750.0,
                RepairKind.WARRANTY, DAY, RepairStatus.NEW);
        assertEquals(RepairKind.WARRANTY, RepairOrderRecord.fromOrder(warranty).kind());
    }

    @Test
    void invariantsAreChecked() {
        assertThrows(IllegalArgumentException.class,
                () -> new RepairOrderRecord(" ", "П", "Н", DAY, 1, RepairKind.PAID));
        assertThrows(IllegalArgumentException.class,
                () -> new RepairOrderRecord("К", "П", "", DAY, 1, RepairKind.PAID));
        assertThrows(IllegalArgumentException.class,
                () -> new RepairOrderRecord("К", "П", "Н", DAY, -1, RepairKind.PAID));
        assertThrows(IllegalArgumentException.class,
                () -> new RepairOrderRecord("К", "П", "Н", DAY, Double.NaN, RepairKind.PAID));
        assertThrows(NullPointerException.class, () -> new RepairOrderRecord("К", "П", "Н", null, 1, RepairKind.PAID));
        assertThrows(NullPointerException.class, () -> new RepairOrderRecord("К", "П", "Н", DAY, 1, null));
    }

    @Test
    void zeroCostIsAccepted() {
        assertEquals(0.0, new RepairOrderRecord("К", "П", "Н", DAY, 0.0, RepairKind.WARRANTY).cost(), 0.0);
    }

    @Test
    void equalityAndHashCode() {
        assertEquals(sample(), sample());
        assertEquals(sample().hashCode(), sample().hashCode());
        assertNotEquals(sample(), new RepairOrderRecord("Іваненко Петро", "Ноутбук Lenovo", "Заміна термопасти",
                DAY.plusDays(1), 650.0, RepairKind.PAID));
        assertFalse(sample().equals(null));
    }

    @Test
    void keyIsCustomerAndDate() {
        assertTrue(sample().hasKey("Іваненко Петро", DAY));
        assertFalse(sample().hasKey("Іваненко Петро", DAY.plusDays(1)));
        assertFalse(sample().hasKey("Коваль Оксана", DAY));
    }

    @Test
    void fromFieldsRestoresRecord() throws DataStorageException {
        assertEquals(sample(), RepairOrderRecord.fromFields(fields("650.0", "2026-01-05", "PAID")));
    }

    @Test
    void wrongFieldCount() {
        DataStorageException e = assertThrows(DataStorageException.class,
                () -> RepairOrderRecord.fromFields(List.of("a", "b")));
        assertEquals("очікується 6 полів, отримано 2", e.getMessage());
    }

    @Test
    void wrongNumberKeepsCause() {
        DataStorageException e = assertThrows(DataStorageException.class,
                () -> RepairOrderRecord.fromFields(fields("abc", "2026-01-05", "PAID")));
        assertEquals("Некоректне число в полі «вартість»: abc", e.getMessage());
        assertInstanceOf(NumberFormatException.class, e.getCause());
    }

    @Test
    void wrongDateKeepsCause() {
        DataStorageException e = assertThrows(DataStorageException.class,
                () -> RepairOrderRecord.fromFields(fields("650", "05.01.2026", "PAID")));
        assertInstanceOf(DateTimeParseException.class, e.getCause());
    }

    @Test
    void wrongEnumKeepsCause() {
        DataStorageException e = assertThrows(DataStorageException.class,
                () -> RepairOrderRecord.fromFields(fields("650", "2026-01-05", "ОРЕНДА")));
        assertEquals("Невідомий вид заявки: ОРЕНДА", e.getMessage());
        assertInstanceOf(IllegalArgumentException.class, e.getCause());
    }

    @Test
    void brokenInvariantInFile() {
        DataStorageException e = assertThrows(DataStorageException.class,
                () -> RepairOrderRecord.fromFields(fields("-2", "2026-01-05", "PAID")));
        assertTrue(e.getMessage().startsWith("Порушено інваріант"));
        assertInstanceOf(IllegalArgumentException.class, e.getCause());
    }
}
