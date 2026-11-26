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

    private String run(Path input, Path output) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes, true, StandardCharsets.UTF_8);
        PrintStream err = new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8);
        assertEquals(Main.EXIT_OK, Main.run(new String[] {"--input", input.toString(),
            "--output", output.toString()}, out, err));
        return bytes.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }

    @Test
    void lab03DataShowsBothKindsUniqueOrdersAndFinalCost() {
        String text = run(Path.of("data", "input-lab03.csv"), tmp.resolve("r.txt"));
        assertTrue(text.contains("Кількість коректних записів                 5"));
        assertTrue(text.contains("Заявок: гарантійний                         2"));
        assertTrue(text.contains("Заявок: платний                             3"));
        assertTrue(text.contains("Унікальних заявок                           4"));
        assertTrue(text.contains("Остаточна вартість, грн               4340.00"));
        assertTrue(text.contains("Рядок 6: hours має бути більшим за 0"));
        assertTrue(text.contains("Рядок 7: невідомий вид ремонту \"ОРЕНДА\""));
    }

    @Test
    void polymorphicBlockIsNotWrittenToReportFile() throws IOException {
        Path report = tmp.resolve("r.txt");
        run(Path.of("data", "input-lab03.csv"), report);
        assertTrue(!Files.readString(report, StandardCharsets.UTF_8).contains("Поліморфні"));
    }

    @Test
    void lab01FileGivesOnlyPaidOrders() {
        String text = run(Path.of("data", "input.csv"), tmp.resolve("r.txt"));
        assertTrue(text.contains("Заявок: платний                             6"));
        assertTrue(!text.contains("Заявок: гарантійний"));
    }

    @Test
    void versionMatchesProject() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        Main.run(new String[] {"--version"}, new PrintStream(bytes, true, StandardCharsets.UTF_8), System.err);
        assertTrue(bytes.toString(StandardCharsets.UTF_8).startsWith("lab01 5.0.0"));
    }
}
