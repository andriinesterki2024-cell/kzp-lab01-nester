package ua.lpnu.kzp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Ізолює кросплатформне читання та запис текстових файлів у кодуванні UTF-8. */
public final class FileReport {

    /* Забороняє створення екземплярів службового класу. */
    private FileReport() {
    }

    /**
     * Читає всі рядки файла у фіксованому кодуванні UTF-8.
     *
     * @param input шлях до вхідного файла
     * @return рядки файла
     * @throws IOException якщо файл неможливо прочитати
     */
    public static List<String> readLines(Path input) throws IOException {
        // Явне кодування не залежить від налаштувань операційної системи.
        return Files.readAllLines(input, StandardCharsets.UTF_8);
    }

    /**
     * Створює батьківський каталог (якщо потрібно) і записує звіт у файл.
     *
     * @param output шлях до файла звіту
     * @param report готовий текст звіту
     * @throws IOException якщо каталог або файл неможливо створити
     */
    public static void writeReport(Path output, String report) throws IOException {
        // Для шляху без каталогу (наприклад, "report.txt") getParent() повертає null.
        Path parent = output.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(output, report, StandardCharsets.UTF_8);
    }
}
