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

/** Перевіряє консольний блок «Запити Stream API» і параметри --top, --find-client. */
class MainStreamTest {

    @TempDir
    Path tmp;

    private final ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errBytes = new ByteArrayOutputStream();

    private int run(String... args) {
        return Main.run(args, new PrintStream(outBytes, true, StandardCharsets.UTF_8),
                new PrintStream(errBytes, true, StandardCharsets.UTF_8));
    }

    private String out() {
        return outBytes.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }

    @Test
    void streamBlockForLab04Data() {
        assertEquals(Main.EXIT_OK, run("--input", "data/input-lab04.csv", "--output", tmp.resolve("r.txt").toString(),
                "--find-client", "Коваль Оксана"));
        String text = out();
        assertTrue(text.contains("Незавершені заявки: 4"));
        assertTrue(text.contains("Вартість за видом: гарантійний=8150.00, платний=8450.00"));
        assertTrue(text.contains("Вартість: count=7, min=300.00, max=5400.00, avg=2371.43, sum=16600.00"));
        assertTrue(text.contains("Топ-5 найдорожчих"));
        assertTrue(text.contains("Пошук клієнта «Коваль Оксана»: Смартфон Samsung, 2026-01-08, 3200.00 грн, у роботі"));
    }

    @Test
    void topOptionChangesN() {
        run("--input", "data/input-lab04.csv", "--output", tmp.resolve("r.txt").toString(), "--top", "1");
        String text = out();
        assertTrue(text.contains("Топ-1 найдорожчих"));
        assertTrue(text.contains("  5400.00 грн, 2026-01-02, Шевчук Андрій, Холодильник LG"));
        assertTrue(!text.contains("  3200.00 грн"));
    }

    @Test
    void missingClientGivesNotFound() {
        run("--input", "data/input-lab04.csv", "--output", tmp.resolve("r.txt").toString(), "--find-client", "Хтось");
        assertTrue(out().contains("Пошук клієнта «Хтось»: не знайдено"));
    }

    @Test
    void invalidTopIsRejected() {
        assertEquals(Main.EXIT_ERROR, run("--top", "-1"));
        assertEquals(Main.EXIT_ERROR, run("--top", "п'ять"));
        assertTrue(errBytes.toString(StandardCharsets.UTF_8).contains("--top"));
    }

    @Test
    void emptyFileShowsNoDataInStatistics() throws IOException {
        Path empty = Files.writeString(tmp.resolve("empty.csv"), "", StandardCharsets.UTF_8);
        run("--input", empty.toString(), "--output", tmp.resolve("r.txt").toString());
        assertTrue(out().contains("Вартість: немає даних"));
        assertTrue(out().contains("Незавершені заявки: 0"));
    }

    @Test
    void reportFileForLab01DataStillMatchesBaseline() throws IOException {
        Path report = tmp.resolve("r.txt");
        run("--input", "baseline/input-lab01.csv", "--output", report.toString());
        assertEquals(Files.readString(Path.of("baseline", "report-lab01.txt"), StandardCharsets.UTF_8).replace("\r\n", "\n"),
                Files.readString(report, StandardCharsets.UTF_8).replace("\r\n", "\n"));
    }
}
