package ua.lpnu.kzp.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ua.lpnu.kzp.DeviceKind;

/** Перевіряє запис у файл і читання назад (round-trip) та помилки читання. */
class RepairCsvRoundTripTest {

    @TempDir
    Path tmp;

    private static final LocalDate DAY = LocalDate.of(2026, 1, 1);

    private static List<RepairRecord> tricky() {
        return List.of(
                new RepairRecord(1, "Іваненко Петро", "Ноутбук Lenovo", DeviceKind.LAPTOP, DAY, 1.5, 650.0),
                new RepairRecord(2, "О'Коннор, \"Шон\" мол.", "Смартфон", DeviceKind.PHONE, DAY.plusDays(1), 2.0, 3200.0),
                new RepairRecord(3, "Бондар\nМарія", "Пральна машина", DeviceKind.WASHER, DAY.plusDays(2), 4.5, 2750.0),
                new RepairRecord(4, "Коваль Оксана", "Планшет", DeviceKind.TABLET, DAY.plusDays(3), 0.5, 0.0),
                new RepairRecord(5, "Шевчук Андрій", "Холодильник", DeviceKind.FRIDGE, DAY.plusDays(4), 6.0, 5400.0),
                new RepairRecord(6, "Лисенко Тарас", "Електросамокат", DeviceKind.OTHER, DAY.plusDays(5), 1.75, 1100.0));
    }

    private Path write(String content) throws IOException {
        Path file = tmp.resolve("r.csv");
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return file;
    }

    private static String header() throws DataStorageException {
        return String.join(",", AnnotatedFields.columns(RepairRecord.class));
    }

    @Test
    void roundTripThroughFileRestoresEqualObjects() throws Exception {
        Path file = tmp.resolve("repairs.csv");
        CsvExporter.write(file, RepairRecord.class, tricky());
        List<RepairRecord> restored = RepairCsvReader.read(file);
        assertEquals(tricky(), restored);
        // Поле з перенесенням рядка: фізичних рядків більше, ніж CSV-записів.
        assertEquals(8, Files.readAllLines(file, StandardCharsets.UTF_8).size());
        assertEquals(7, CsvParser.parseDocument(Files.readString(file, StandardCharsets.UTF_8)).size());
    }

    @Test
    void searchByKeyAfterRestore() throws Exception {
        Path file = tmp.resolve("repairs.csv");
        CsvExporter.write(file, RepairRecord.class, tricky());
        Repository<RepairRecord> repo = new Repository<>();
        repo.addAll(RepairCsvReader.read(file));
        assertEquals(1, repo.find(r -> r.hasKey(2)).size());
        assertEquals("О'Коннор, \"Шон\" мол.", repo.find(r -> r.hasKey(2)).get(0).client());
    }

    @Test
    void emptyFileAndHeaderOnlyGiveEmptyList() throws Exception {
        assertTrue(RepairCsvReader.read(write("")).isEmpty());
        assertTrue(RepairCsvReader.read(write(header() + "\n")).isEmpty());
    }

    @Test
    void emptyListRoundTrip() throws Exception {
        Path file = tmp.resolve("empty.csv");
        CsvExporter.write(file, RepairRecord.class, List.of());
        assertTrue(RepairCsvReader.read(file).isEmpty());
    }

    @Test
    void crlfFileIsReadOnAnyOs() throws Exception {
        String csv = CsvExporter.toCsv(RepairRecord.class, tricky().subList(0, 2)).replace(System.lineSeparator(), "\r\n");
        assertEquals(tricky().subList(0, 2), RepairCsvReader.read(write(csv)));
    }

    @Test
    void unknownHeaderIsRejected() {
        DataStorageException e = assertThrows(DataStorageException.class,
                () -> RepairCsvReader.read(write("a,b,c\n1,2,3\n")));
        assertTrue(e.getMessage().startsWith("Невідомий заголовок CSV"));
    }

    @Test
    void brokenNumberHasRecordNumberAndChainOfCauses() throws Exception {
        String csv = header() + "\n2026-01-01,Іван,Ноутбук,1.5,LAPTOP,650,1\n2026-01-02,Петро,Смартфон,2,PHONE,abc,2\n";
        DataStorageException e = assertThrows(DataStorageException.class, () -> RepairCsvReader.read(write(csv)));
        assertEquals("CSV-запис 2: Некоректне число в полі «вартість»: abc", e.getMessage());
        assertInstanceOf(DataStorageException.class, e.getCause());
        assertInstanceOf(NumberFormatException.class, e.getCause().getCause());
    }

    @Test
    void brokenFileFromDataFolder() {
        DataStorageException e = assertThrows(DataStorageException.class,
                () -> RepairCsvReader.read(Path.of("data", "repairs-broken.csv")));
        assertEquals("CSV-запис 2: Некоректне число в полі «вартість»: abc", e.getMessage());
        assertInstanceOf(NumberFormatException.class, e.getCause().getCause());
    }

    @Test
    void repeatedTicketIsFoundOnceByFullKey() throws Exception {
        List<RepairRecord> twice = List.of(tricky().get(0),
                new RepairRecord(1, "Іваненко Петро", "Ноутбук Lenovo", DeviceKind.LAPTOP, DAY.plusDays(9), 1.0, 300.0));
        Path file = tmp.resolve("twice.csv");
        CsvExporter.write(file, RepairRecord.class, twice);
        List<RepairRecord> restored = RepairCsvReader.read(file);
        assertEquals(twice, restored);
        assertEquals(2, restored.stream().filter(r -> r.hasKey(1)).count());
        assertEquals(1, restored.stream().filter(r -> r.hasKey(1, DAY)).count());
    }

    @Test
    void wrongFieldCountInFile() throws Exception {
        String csv = header() + "\n2026-01-01,Іван,Ноутбук\n";
        DataStorageException e = assertThrows(DataStorageException.class, () -> RepairCsvReader.read(write(csv)));
        assertTrue(e.getMessage().contains("очікується 7 полів"));
    }

    @Test
    void unclosedQuoteInFile() {
        assertThrows(DataStorageException.class, () -> RepairCsvReader.read(write(header() + "\n\"незакрито\n")));
    }

    @Test
    void missingFileKeepsIoCause() {
        DataStorageException e = assertThrows(DataStorageException.class,
                () -> RepairCsvReader.read(tmp.resolve("nope.csv")));
        assertInstanceOf(NoSuchFileException.class, e.getCause());
    }

    @Test
    void ukrainianTextSurvivesUtf8() throws Exception {
        Path file = tmp.resolve("u.csv");
        CsvExporter.write(file, RepairRecord.class, tricky());
        assertTrue(Files.readString(file, StandardCharsets.UTF_8).contains("квитанція"));
        assertEquals("Іваненко Петро", RepairCsvReader.read(file).get(0).client());
    }
}
