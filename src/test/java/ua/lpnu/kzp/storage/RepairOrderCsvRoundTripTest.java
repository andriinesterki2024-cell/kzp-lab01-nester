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
import ua.lpnu.kzp.RepairKind;

/** Перевіряє запис у файл і читання назад (round-trip) та помилки читання. */
class RepairOrderCsvRoundTripTest {

    @TempDir
    Path tmp;

    private static final LocalDate DAY = LocalDate.of(2026, 1, 2);

    private static List<RepairOrderRecord> tricky() {
        return List.of(
                new RepairOrderRecord("Іваненко Петро", "Ноутбук Lenovo", "Заміна термопасти", DAY, 650.0, RepairKind.PAID),
                new RepairOrderRecord("О'Коннор, \"Шон\" мол.", "Смартфон", "Заміна екрана", DAY.plusDays(1), 3200.0,
                        RepairKind.PAID),
                new RepairOrderRecord("Бондар\nМарія", "Пральна машина", "Заміна підшипника", DAY.plusDays(2), 2750.0,
                        RepairKind.WARRANTY),
                new RepairOrderRecord("Коваль Оксана", "Планшет", "Діагностика", DAY.plusDays(3), 0.0, RepairKind.WARRANTY),
                new RepairOrderRecord("Шевчук Андрій", "Холодильник", "Заміна компресора", DAY.plusDays(4), 5400.0,
                        RepairKind.WARRANTY),
                new RepairOrderRecord("Коваль Оксана", "Смартфон", "Заміна акумулятора", DAY.plusDays(5), 1100.0,
                        RepairKind.PAID));
    }

    private Path write(String content) throws IOException {
        Path file = tmp.resolve("r.csv");
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return file;
    }

    private static String header() throws DataStorageException {
        return String.join(",", AnnotatedFields.columns(RepairOrderRecord.class));
    }

    @Test
    void roundTripThroughFileRestoresEqualObjects() throws Exception {
        Path file = tmp.resolve("orders.csv");
        CsvExporter.write(file, RepairOrderRecord.class, tricky());
        List<RepairOrderRecord> restored = RepairOrderCsvReader.read(file);
        assertEquals(tricky(), restored);
        // Поле з перенесенням рядка: фізичних рядків більше, ніж CSV-записів.
        assertEquals(8, Files.readAllLines(file, StandardCharsets.UTF_8).size());
        assertEquals(7, CsvParser.parseDocument(Files.readString(file, StandardCharsets.UTF_8)).size());
    }

    @Test
    void searchByKeyCustomerAndDateAfterRestore() throws Exception {
        Path file = tmp.resolve("orders.csv");
        CsvExporter.write(file, RepairOrderRecord.class, tricky());
        Repository<RepairOrderRecord> repo = new Repository<>();
        repo.addAll(RepairOrderCsvReader.read(file));
        // Коваль Оксана має дві заявки: ключ customer + date знаходить рівно одну.
        assertEquals(2, repo.find(r -> r.customer().equals("Коваль Оксана")).size());
        List<RepairOrderRecord> one = repo.find(r -> r.hasKey("Коваль Оксана", DAY.plusDays(5)));
        assertEquals(1, one.size());
        assertEquals("Смартфон", one.get(0).device());
    }

    @Test
    void headerFromAnnotations() throws DataStorageException {
        assertEquals("вартість,клієнт,дата,пристрій,несправність,вид", header());
    }

    @Test
    void emptyFileAndHeaderOnlyGiveEmptyList() throws Exception {
        assertTrue(RepairOrderCsvReader.read(write("")).isEmpty());
        assertTrue(RepairOrderCsvReader.read(write(header() + "\n")).isEmpty());
    }

    @Test
    void emptyListRoundTrip() throws Exception {
        Path file = tmp.resolve("empty.csv");
        CsvExporter.write(file, RepairOrderRecord.class, List.of());
        assertTrue(RepairOrderCsvReader.read(file).isEmpty());
    }

    @Test
    void crlfFileIsReadOnAnyOs() throws Exception {
        String csv = CsvExporter.toCsv(RepairOrderRecord.class, tricky().subList(0, 2))
                .replace(System.lineSeparator(), "\r\n");
        assertEquals(tricky().subList(0, 2), RepairOrderCsvReader.read(write(csv)));
    }

    @Test
    void unknownHeaderIsRejected() {
        DataStorageException e = assertThrows(DataStorageException.class,
                () -> RepairOrderCsvReader.read(write("a,b,c\n1,2,3\n")));
        assertTrue(e.getMessage().startsWith("Невідомий заголовок CSV"));
    }

    @Test
    void brokenFileFromDataFolderHasRecordNumberAndChainOfCauses() {
        DataStorageException e = assertThrows(DataStorageException.class,
                () -> RepairOrderCsvReader.read(Path.of("data", "orders-broken.csv")));
        assertEquals("CSV-запис 2: Некоректне число в полі «вартість»: abc", e.getMessage());
        assertInstanceOf(DataStorageException.class, e.getCause());
        assertInstanceOf(NumberFormatException.class, e.getCause().getCause());
    }

    @Test
    void wrongEnumInFile() throws Exception {
        String csv = header() + "\n650.0,Іван,2026-01-02,Ноутбук,Ремонт,ОРЕНДА\n";
        DataStorageException e = assertThrows(DataStorageException.class, () -> RepairOrderCsvReader.read(write(csv)));
        assertEquals("CSV-запис 1: Невідомий вид заявки: ОРЕНДА", e.getMessage());
    }

    @Test
    void wrongFieldCountInFile() throws Exception {
        String csv = header() + "\n650.0,Іван,2026-01-02\n";
        DataStorageException e = assertThrows(DataStorageException.class, () -> RepairOrderCsvReader.read(write(csv)));
        assertTrue(e.getMessage().contains("очікується 6 полів"));
    }

    @Test
    void unclosedQuoteInFile() {
        assertThrows(DataStorageException.class, () -> RepairOrderCsvReader.read(write(header() + "\n\"незакрито\n")));
    }

    @Test
    void missingFileKeepsIoCause() {
        DataStorageException e = assertThrows(DataStorageException.class,
                () -> RepairOrderCsvReader.read(tmp.resolve("nope.csv")));
        assertInstanceOf(NoSuchFileException.class, e.getCause());
    }

    @Test
    void ukrainianTextSurvivesUtf8() throws Exception {
        Path file = tmp.resolve("u.csv");
        CsvExporter.write(file, RepairOrderRecord.class, tricky());
        assertTrue(Files.readString(file, StandardCharsets.UTF_8).contains("несправність"));
        assertEquals("Іваненко Петро", RepairOrderCsvReader.read(file).get(0).customer());
    }
}
