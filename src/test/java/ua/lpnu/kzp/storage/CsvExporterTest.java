package ua.lpnu.kzp.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Перевіряє універсальний експортер на сторонніх класах (без предметних назв). */
class CsvExporterTest {

    @TempDir
    Path tmp;

    /** Сторонній клас: експортер про нього нічого не знає. */
    static class Place {
        @CsvColumn("назва")
        private final String name;
        private final String secret = "не експортується";

        Place(String name) {
            this.name = name;
        }
    }

    /** Підклас з власним полем: успадковане поле теж експортується. */
    static final class City extends Place {
        @CsvColumn("населення")
        private final int population;

        City(String name, int population) {
            super(name);
            this.population = population;
        }
    }

    static final class NoColumns {
        private final int x = 1;
    }

    private static String nl() {
        return System.lineSeparator();
    }

    @Test
    void headerAndRowsForThirdPartyClass() throws DataStorageException {
        String csv = CsvExporter.toCsv(City.class, List.of(new City("Львів", 717_000), new City("Київ", 2_952_000)));
        assertEquals("назва,населення" + nl() + "Львів,717000" + nl() + "Київ,2952000" + nl(), csv);
    }

    @Test
    void unannotatedFieldIsNotExported() throws DataStorageException {
        assertTrue(!CsvExporter.toCsv(Place.class, List.of(new Place("А"))).contains("не експортується"));
    }

    @Test
    void emptyListStillHasHeader() throws DataStorageException {
        assertEquals("назва,населення" + nl(), CsvExporter.toCsv(City.class, List.of()));
    }

    @Test
    void columnsAreSortedByFieldName() throws DataStorageException {
        List<String> names = AnnotatedFields.of(RepairOrderRecord.class).stream().map(Field::getName).toList();
        assertEquals(List.of("cost", "customer", "date", "device", "fault", "kind"), names);
    }

    @Test
    void fieldListIsCached() throws DataStorageException {
        assertSame(AnnotatedFields.of(City.class), AnnotatedFields.of(City.class));
        assertEquals(AnnotatedFields.of(City.class), AnnotatedFields.scan(City.class));
    }

    @Test
    void escapeCommaQuoteAndNewline() {
        assertEquals("просто", CsvExporter.escape("просто"));
        assertEquals("\"Петренко, Іван\"", CsvExporter.escape("Петренко, Іван"));
        assertEquals("\"О\"\"Ніл\"", CsvExporter.escape("О\"Ніл"));
        assertEquals("\"два\nрядки\"", CsvExporter.escape("два\nрядки"));
        assertEquals("", CsvExporter.escape(null));
    }

    @Test
    void classWithoutColumnsIsAnError() {
        DataStorageException e = assertThrows(DataStorageException.class,
                () -> CsvExporter.toCsv(NoColumns.class, List.of(new NoColumns())));
        assertTrue(e.getMessage().contains("@CsvColumn"));
    }

    @Test
    void writesUtf8WithoutBom() throws Exception {
        Path file = tmp.resolve("sub").resolve("c.csv");
        CsvExporter.write(file, City.class, List.of(new City("Житомир", 261_000)));
        byte[] bytes = Files.readAllBytes(file);
        assertTrue(bytes[0] != (byte) 0xEF);
        assertTrue(new String(bytes, StandardCharsets.UTF_8).contains("Житомир"));
    }

    @Test
    void shorterExportTruncatesOldContent() throws Exception {
        Path file = tmp.resolve("c.csv");
        CsvExporter.write(file, City.class, List.of(new City("А", 1), new City("Б", 2), new City("В", 3)));
        CsvExporter.write(file, City.class, List.of(new City("Г", 4)));
        assertEquals(2, Files.readAllLines(file, StandardCharsets.UTF_8).size());
    }

    @Test
    void writeToDirectoryKeepsIoCause() throws IOException {
        Path dir = Files.createDirectory(tmp.resolve("dir"));
        DataStorageException e = assertThrows(DataStorageException.class,
                () -> CsvExporter.write(dir, City.class, List.of()));
        assertInstanceOf(IOException.class, e.getCause());
    }
}
