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

/** Перевіряє --export і --export-json у Main та звіт про помилку експорту. */
class MainExportTest {

    @TempDir
    Path tmp;

    private final ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
    private final PrintStream out = new PrintStream(outBytes, true, StandardCharsets.UTF_8);
    private final PrintStream err = new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8);

    private String stdout() {
        return outBytes.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }

    private int run(String... extra) {
        String[] base = {"--input", Path.of("data", "input-lab05.csv").toString(),
            "--output", tmp.resolve("report.txt").toString()};
        String[] all = new String[base.length + extra.length];
        System.arraycopy(base, 0, all, 0, base.length);
        System.arraycopy(extra, 0, all, base.length, extra.length);
        return Main.run(all, out, err);
    }

    @Test
    void exportAndRoundTrip() throws IOException {
        Path csv = tmp.resolve("out").resolve("orders.csv");
        assertEquals(Main.EXIT_OK, run("--export", csv.toString()));
        String text = stdout();
        assertTrue(text.contains("Заголовок: вартість,клієнт,дата,пристрій,несправність,вид"));
        assertTrue(text.contains("Експортовано записів: 6"));
        assertTrue(text.contains("Відновлено записів: 6"));
        assertTrue(text.contains("Round-trip рівність: true"));
        assertTrue(Files.readString(csv, StandardCharsets.UTF_8).contains("\"О'Коннор, \"\"Шон\"\" мол.\""));
    }

    @Test
    void jsonExport() throws IOException {
        Path json = tmp.resolve("orders.json");
        assertEquals(Main.EXIT_OK, run("--export-json", json.toString()));
        assertTrue(Files.readString(json, StandardCharsets.UTF_8).contains("\"вид\": \"WARRANTY\""));
        assertTrue(stdout().contains("JSON збережено"));
    }

    @Test
    void exportFailureIsReportedWithCauseAndReportIsStillSaved() throws IOException {
        Path dir = Files.createDirectory(tmp.resolve("dir"));
        assertEquals(Main.EXIT_OK, run("--export", dir.toString()));
        assertTrue(stdout().contains("Помилка експорту: Не вдалося записати CSV"));
        assertTrue(stdout().contains("Причина: java.nio.file."));
        assertTrue(Files.exists(tmp.resolve("report.txt")));
    }

    @Test
    void withoutExportOptionsThereIsNoExportBlock() {
        assertEquals(Main.EXIT_OK, run());
        assertTrue(!stdout().contains("CSV-експорт"));
    }
}
