package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Перевіряє запуск програми з аргументами та запис звіту у файл. */
class MainTest {

    @TempDir
    Path tmp;

    private final ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errBytes = new ByteArrayOutputStream();
    private final PrintStream out = new PrintStream(outBytes, true, StandardCharsets.UTF_8);
    private final PrintStream err = new PrintStream(errBytes, true, StandardCharsets.UTF_8);

    private Path writeInput(String... lines) throws IOException {
        Path input = tmp.resolve("input.csv");
        Files.write(input, List.of(lines), StandardCharsets.UTF_8);
        return input;
    }

    @Test
    void helpPrintsUsage() {
        int code = Main.run(new String[] {"--help"}, out, err);
        assertEquals(Main.EXIT_OK, code);
        assertTrue(outBytes.toString(StandardCharsets.UTF_8).contains("--input"));
    }

    @Test
    void versionPrintsProjectVersion() {
        int code = Main.run(new String[] {"--version"}, out, err);
        assertEquals(Main.EXIT_OK, code);
        assertTrue(outBytes.toString(StandardCharsets.UTF_8).startsWith("lab01 5.0.0"));
    }

    @Test
    void reportIsWrittenToNestedMissingDirectory() throws IOException {
        Path input = writeInput(
                "Коваль Оксана;Смартфон Samsung;Заміна екрана;2.0;3200.00",
                "Шевчук Андрій;Принтер HP;Заміна ролика;1.25;800.00");
        Path output = tmp.resolve("a").resolve("b").resolve("report.txt");

        int code = Main.run(new String[] {"--input", input.toString(),
            "--output", output.toString()}, out, err);

        assertEquals(Main.EXIT_OK, code);
        String report = Files.readString(output, StandardCharsets.UTF_8);
        assertTrue(report.contains("4000.00"));
        assertTrue(report.contains("3.25"));
        assertTrue(report.contains("Коваль Оксана"));
        // Консоль і файл отримують той самий текст звіту.
        assertTrue(outBytes.toString(StandardCharsets.UTF_8).contains(report));
    }

    @Test
    void missingInputFileIsReported() {
        Path missing = tmp.resolve("nope.csv");
        int code = Main.run(new String[] {"--input", missing.toString()}, out, err);
        assertEquals(Main.EXIT_ERROR, code);
        assertTrue(errBytes.toString(StandardCharsets.UTF_8).contains("не знайдено"));
    }

    @Test
    void unknownArgumentAndMissingValueAreErrors() {
        assertEquals(Main.EXIT_ERROR, Main.run(new String[] {"--bogus"}, out, err));
        assertEquals(Main.EXIT_ERROR, Main.run(new String[] {"--input"}, out, err));
    }

    @Test
    void noValidRecordsGivesSpecialExitCode() throws IOException {
        Path input = writeInput("", "зламаний рядок");
        Path output = tmp.resolve("report.txt");
        int code = Main.run(new String[] {"--input", input.toString(),
            "--output", output.toString()}, out, err);
        assertEquals(Main.EXIT_NO_DATA, code);
        assertTrue(Files.readString(output, StandardCharsets.UTF_8)
                .contains("Коректних записів не знайдено"));
    }
}
