package ua.lpnu.kzp.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import ua.lpnu.kzp.DeviceKind;

/** Перевіряє другий формат і спільний інтерфейс {@link Exporter}. */
class JsonExporterTest {

    private static final RepairRecord R = new RepairRecord(7, "О'Коннор, \"Шон\"", "Смартфон",
            DeviceKind.PHONE, LocalDate.of(2026, 1, 2), 2.0, 3200.0);

    @Test
    void jsonUsesSameAnnotatedFields() throws DataStorageException {
        String json = JsonExporter.toJson(RepairRecord.class, List.of(R));
        assertTrue(json.contains("\"квитанція\": 7"));
        assertTrue(json.contains("\"вартість\": 3200.0"));
        assertTrue(json.contains("\"тип\": \"PHONE\""));
        assertTrue(json.contains("\"клієнт\": \"О'Коннор, \\\"Шон\\\"\""));
    }

    @Test
    void emptyListIsEmptyArray() throws DataStorageException {
        assertEquals("[]" + System.lineSeparator(), JsonExporter.toJson(RepairRecord.class, List.of()));
    }

    @Test
    void bothFormatsThroughCommonInterface() throws DataStorageException {
        Exporter<RepairRecord> csv = CsvExporter::toCsv;
        Exporter<RepairRecord> json = JsonExporter::toJson;
        assertTrue(csv.export(RepairRecord.class, List.of(R)).startsWith("прийнято,клієнт"));
        assertTrue(json.export(RepairRecord.class, List.of(R)).startsWith("["));
    }

    @Test
    void newlineIsEscaped() throws DataStorageException {
        RepairRecord r = new RepairRecord(1, "два\nрядки", "П", DeviceKind.OTHER, LocalDate.of(2026, 1, 1), 1, 1);
        assertTrue(JsonExporter.toJson(RepairRecord.class, List.of(r)).contains("два\\nрядки"));
    }
}
