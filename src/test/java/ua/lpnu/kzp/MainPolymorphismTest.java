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

/** Перевіряє поліморфний блок у консолі та незмінність файла звіту. */
class MainPolymorphismTest {

    @TempDir
    Path tmp;

    private final ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
    private final PrintStream out = new PrintStream(outBytes, true, StandardCharsets.UTF_8);
    private final PrintStream err = new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8);

    private String stdout() {
        return outBytes.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }

    @Test
    void mixedDataPrintsPolymorphicBlock() {
        Path output = tmp.resolve("r.txt");
        int code = Main.run(new String[] {"--input", Path.of("data", "input-lab03.csv").toString(),
            "--output", output.toString()}, out, err);
        assertEquals(Main.EXIT_OK, code);
        String text = stdout();
        assertTrue(text.contains("--- Поліморфні показники ---"));
        assertTrue(text.matches("(?s).*Гаджети\\s+2\n.*"));
        assertTrue(text.matches("(?s).*Побутова техніка\\s+2\n.*"));
        assertTrue(text.matches("(?s).*Унікальних ремонтів\\s+4\n.*"));
        // 650 + 3200 + (2750 + 200) + (5400 + 200) = 12400.
        assertTrue(text.contains("12400.00"));
        assertTrue(text.contains("Рядок 5: порожній рядок"));
        assertTrue(text.contains("Рядок 6: hours має бути більшим за 0"));
    }

    @Test
    void reportFileIsIdenticalToBaselineOfPreviousLab() throws IOException {
        Path output = tmp.resolve("report.txt");
        int code = Main.run(new String[] {"--input", Path.of("baseline", "input-lab01.csv").toString(),
            "--output", output.toString()}, out, err);
        assertEquals(Main.EXIT_OK, code);
        String expected = Files.readString(Path.of("baseline", "report-lab01.txt"), StandardCharsets.UTF_8);
        assertEquals(expected.replace("\r\n", "\n"),
                Files.readString(output, StandardCharsets.UTF_8).replace("\r\n", "\n"));
    }

    @Test
    void polymorphicBlockIsNotWrittenToFile() throws IOException {
        Path output = tmp.resolve("report.txt");
        Main.run(new String[] {"--input", Path.of("data", "input-lab03.csv").toString(),
            "--output", output.toString()}, out, err);
        String file = Files.readString(output, StandardCharsets.UTF_8);
        assertTrue(!file.contains("Поліморфні"));
        assertTrue(file.contains("12000.00"));
    }

    @Test
    void versionMatchesProject() {
        assertEquals(Main.EXIT_OK, Main.run(new String[] {"--version"}, out, err));
        assertTrue(stdout().startsWith("lab01 4.0.0"));
    }
}
