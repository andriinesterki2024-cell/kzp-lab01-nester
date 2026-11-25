package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Перевіряє консольний блок запитів і параметри --min-price, --top, --find-client. */
class MainStreamTest {

    @TempDir
    Path tmp;

    private final ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errBytes = new ByteArrayOutputStream();
    private final PrintStream out = new PrintStream(outBytes, true, StandardCharsets.UTF_8);
    private final PrintStream err = new PrintStream(errBytes, true, StandardCharsets.UTF_8);

    private int run(String... extra) {
        String[] base = {"--input", Path.of("data", "input-lab04.csv").toString(),
            "--output", tmp.resolve("r.txt").toString()};
        String[] all = new String[base.length + extra.length];
        System.arraycopy(base, 0, all, 0, base.length);
        System.arraycopy(extra, 0, all, base.length, extra.length);
        return Main.run(all, out, err);
    }

    private String stdout() {
        return outBytes.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }

    @Test
    void defaultBlockContainsAllQueries() {
        assertEquals(Main.EXIT_OK, run());
        String text = stdout();
        assertTrue(text.contains("--- Запити Stream API ---"));
        assertTrue(text.contains("Ремонти від 1000.00 грн: 5"));
        assertTrue(text.contains("Клієнти: Андрієнко Ірина, Бондар Марія, Іваненко Петро"));
        assertTrue(text.contains("За типом пристрою: LAPTOP=1, PHONE=1, TABLET=1, PRINTER=1, CONSOLE=1, TV=1, WASHER=1"));
        assertTrue(text.contains("Години: count=7, min=0.50, max=4.50, avg=2.04"));
        assertTrue(text.contains("Топ-3 найдорожчих:\n  Андрієнко Ірина, Телевізор LG, 3200.00 грн\n"
                + "  Коваль Оксана, Смартфон Samsung A54, 3200.00 грн\n  Бондар Марія"));
    }

    @Test
    void optionsChangeQueries() {
        assertEquals(Main.EXIT_OK, run("--min-price", "3000", "--top", "1", "--find-client", "коваль оксана"));
        String text = stdout();
        assertTrue(text.contains("Ремонти від 3000.00 грн: 2"));
        assertTrue(text.contains("Топ-1 найдорожчих:\n  Андрієнко Ірина"));
        assertTrue(text.contains("Пошук клієнта «коваль оксана»: Смартфон Samsung A54, 2.00 год, 3200.00 грн"));
    }

    @Test
    void missingClientIsReportedAsNotFound() {
        assertEquals(Main.EXIT_OK, run("--find-client", "Невідомий"));
        assertTrue(stdout().contains("Пошук клієнта «Невідомий»: не знайдено"));
    }

    @Test
    void invalidOptionValuesAreErrors() {
        assertEquals(Main.EXIT_ERROR, run("--top", "-1"));
        assertEquals(Main.EXIT_ERROR, run("--top", "три"));
        assertEquals(Main.EXIT_ERROR, run("--min-price", "NaN"));
        assertEquals(Main.EXIT_ERROR, run("--find-client"));
        assertTrue(errBytes.toString(StandardCharsets.UTF_8).contains("--top"));
    }

    @Test
    void emptyFileGivesNoDataStatistics() throws IOException {
        Path empty = tmp.resolve("empty.csv");
        Files.writeString(empty, "", StandardCharsets.UTF_8);
        int code = Main.run(new String[] {"--input", empty.toString(), "--output", tmp.resolve("e.txt").toString()},
                out, err);
        assertEquals(Main.EXIT_NO_DATA, code);
        assertTrue(stdout().contains("Години: немає даних"));
        assertTrue(stdout().contains("Клієнти: \n"));
    }

    @Test
    void reportFileStillMatchesBaseline() throws IOException {
        Path output = tmp.resolve("report.txt");
        Main.run(new String[] {"--input", Path.of("baseline", "input-lab01.csv").toString(),
            "--output", output.toString()}, out, err);
        assertEquals(Files.readString(Path.of("baseline", "report-lab01.txt"), StandardCharsets.UTF_8).replace("\r\n", "\n"),
                Files.readString(output, StandardCharsets.UTF_8).replace("\r\n", "\n"));
    }
}
