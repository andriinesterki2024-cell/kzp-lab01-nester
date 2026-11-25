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
import ua.lpnu.kzp.DeviceKind;
import ua.lpnu.kzp.Repair;

/** Перевіряє запис варіанта: інваріанти, рівність, ключ, розбір полів і причини помилок. */
class RepairRecordTest {

    private static final LocalDate DAY = LocalDate.of(2026, 1, 1);

    private static RepairRecord sample() {
        return new RepairRecord(1, "Іваненко Петро", "Ноутбук Lenovo", DeviceKind.LAPTOP, DAY, 1.5, 650.0);
    }

    private static List<String> fields(String date, String hours, String kind, String price, String ticket) {
        return List.of(date, "Іваненко Петро", "Ноутбук Lenovo", hours, kind, price, ticket);
    }

    @Test
    void fromRepairCopiesFields() {
        RepairRecord r = RepairRecord.fromRepair(Repair.create("Іваненко Петро", "Ноутбук Lenovo", "Т", 1.5, 650), 1, DAY);
        assertEquals(sample(), r);
    }

    @Test
    void invariantsAreChecked() {
        assertThrows(IllegalArgumentException.class, () -> new RepairRecord(0, "К", "П", DeviceKind.OTHER, DAY, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new RepairRecord(1, " ", "П", DeviceKind.OTHER, DAY, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new RepairRecord(1, "К", "П", DeviceKind.OTHER, DAY, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new RepairRecord(1, "К", "П", DeviceKind.OTHER, DAY, 1, -1));
        assertThrows(NullPointerException.class, () -> new RepairRecord(1, "К", "П", null, DAY, 1, 1));
    }

    @Test
    void boundaryValuesAreAccepted() {
        RepairRecord r = new RepairRecord(1, "К", "П", DeviceKind.OTHER, DAY, 0.01, 0.0);
        assertEquals(0.0, r.price(), 0.0);
    }

    @Test
    void equalityAndHashCode() {
        assertEquals(sample(), sample());
        assertEquals(sample().hashCode(), sample().hashCode());
        assertNotEquals(sample(), new RepairRecord(2, "Іваненко Петро", "Ноутбук Lenovo", DeviceKind.LAPTOP, DAY, 1.5, 650));
        assertFalse(sample().equals(null));
    }

    @Test
    void keyIsTicket() {
        assertTrue(sample().hasKey(1));
        assertFalse(sample().hasKey(2));
    }

    @Test
    void fromFieldsRestoresRecord() throws DataStorageException {
        assertEquals(sample(), RepairRecord.fromFields(fields("2026-01-01", "1.5", "LAPTOP", "650.0", "1")));
    }

    @Test
    void wrongFieldCount() {
        DataStorageException e = assertThrows(DataStorageException.class,
                () -> RepairRecord.fromFields(List.of("a", "b")));
        assertEquals("очікується 7 полів, отримано 2", e.getMessage());
    }

    @Test
    void wrongNumberKeepsCause() {
        DataStorageException e = assertThrows(DataStorageException.class,
                () -> RepairRecord.fromFields(fields("2026-01-01", "1.5", "LAPTOP", "abc", "1")));
        assertEquals("Некоректне число в полі «вартість»: abc", e.getMessage());
        assertInstanceOf(NumberFormatException.class, e.getCause());
    }

    @Test
    void wrongDateKeepsCause() {
        DataStorageException e = assertThrows(DataStorageException.class,
                () -> RepairRecord.fromFields(fields("01.01.2026", "1.5", "LAPTOP", "650", "1")));
        assertInstanceOf(DateTimeParseException.class, e.getCause());
    }

    @Test
    void wrongEnumKeepsCause() {
        DataStorageException e = assertThrows(DataStorageException.class,
                () -> RepairRecord.fromFields(fields("2026-01-01", "1.5", "ТОСТЕР", "650", "1")));
        assertEquals("Невідомий тип пристрою: ТОСТЕР", e.getMessage());
        assertInstanceOf(IllegalArgumentException.class, e.getCause());
    }

    @Test
    void brokenInvariantInFile() {
        DataStorageException e = assertThrows(DataStorageException.class,
                () -> RepairRecord.fromFields(fields("2026-01-01", "-2", "LAPTOP", "650", "1")));
        assertTrue(e.getMessage().startsWith("Порушено інваріант"));
        assertInstanceOf(IllegalArgumentException.class, e.getCause());
    }
}
