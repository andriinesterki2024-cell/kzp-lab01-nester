package ua.lpnu.kzp.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import ua.lpnu.kzp.RepairKind;

/** Перевіряє другий формат і спільний інтерфейс {@link Exporter}. */
class JsonExporterTest {

    private static final RepairOrderRecord R = new RepairOrderRecord("О'Коннор, \"Шон\"", "Смартфон",
            "Заміна екрана", LocalDate.of(2026, 1, 2), 3200.0, RepairKind.PAID);

    @Test
    void jsonUsesSameAnnotatedFields() throws DataStorageException {
        String json = JsonExporter.toJson(RepairOrderRecord.class, List.of(R));
        assertTrue(json.contains("\"дата\": \"2026-01-02\""));
        assertTrue(json.contains("\"вартість\": 3200.0"));
        assertTrue(json.contains("\"вид\": \"PAID\""));
        assertTrue(json.contains("\"клієнт\": \"О'Коннор, \\\"Шон\\\"\""));
    }

    @Test
    void emptyListIsEmptyArray() throws DataStorageException {
        assertEquals("[]" + System.lineSeparator(), JsonExporter.toJson(RepairOrderRecord.class, List.of()));
    }

    @Test
    void bothFormatsThroughCommonInterface() throws DataStorageException {
        Exporter<RepairOrderRecord> csv = CsvExporter::toCsv;
        Exporter<RepairOrderRecord> json = JsonExporter::toJson;
        assertTrue(csv.export(RepairOrderRecord.class, List.of(R)).startsWith("вартість,клієнт,дата"));
        assertTrue(json.export(RepairOrderRecord.class, List.of(R)).startsWith("["));
    }

    @Test
    void newlineIsEscaped() throws DataStorageException {
        RepairOrderRecord r = new RepairOrderRecord("два\nрядки", "П", "Н", LocalDate.of(2026, 1, 1), 1, RepairKind.PAID);
        assertTrue(JsonExporter.toJson(RepairOrderRecord.class, List.of(r)).contains("два\\nрядки"));
    }
}
