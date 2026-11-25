package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * Доводить, що рефакторинг не змінив зовнішню поведінку: звіт для контрольного
 * файла лабораторної роботи № 1 збігається зі збереженим еталоном.
 */
class BaselineCompatibilityTest {

    @Test
    void reportMatchesLab01Baseline() throws IOException {
        Path input = Path.of("baseline", "input-lab01.csv");
        Path expected = Path.of("baseline", "report-lab01.txt");

        String actual = ReportFormatter.format(RepairAnalyzer.analyze(FileReport.readLines(input)));

        // Роздільник рядків залежить від ОС, тому порівнюємо після нормалізації до \n.
        assertEquals(normalize(Files.readString(expected, StandardCharsets.UTF_8)), normalize(actual));
    }

    private static String normalize(String text) {
        return text.replace("\r\n", "\n");
    }
}
