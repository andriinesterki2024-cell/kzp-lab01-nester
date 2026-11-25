package ua.lpnu.kzp.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Читає CSV, створений {@link CsvExporter}, і відновлює записи {@link RepairRecord}.
 */
public final class RepairCsvReader {

    /* Забороняє створення екземплярів службового класу. */
    private RepairCsvReader() {
    }

    /**
     * Читає файл з UTF-8, перевіряє заголовок і відновлює записи.
     * Порожній файл і файл лише із заголовком дають порожній список.
     *
     * @param path шлях до CSV
     * @return записи в порядку файла
     * @throws DataStorageException якщо файл неможливо прочитати або дані некоректні;
     *                              повідомлення містить номер запису, причина — першопричину
     */
    public static List<RepairRecord> read(Path path) throws DataStorageException {
        String text;
        try {
            text = Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new DataStorageException("Не вдалося прочитати CSV: " + path, e);
        }
        List<List<String>> rows = CsvParser.parseDocument(text);
        if (rows.isEmpty()) {
            return List.of();
        }
        List<String> expected = AnnotatedFields.columns(RepairRecord.class);
        if (!rows.get(0).equals(expected)) {
            throw new DataStorageException("Невідомий заголовок CSV: " + rows.get(0) + ", очікується " + expected);
        }
        List<RepairRecord> records = new ArrayList<>();
        for (int index = 1; index < rows.size(); index++) {
            try {
                records.add(RepairRecord.fromFields(rows.get(index)));
            } catch (DataStorageException e) {
                // Додається контекст (номер запису); першопричина не губиться.
                throw new DataStorageException(String.format("CSV-запис %d: %s", index, e.getMessage()), e);
            }
        }
        return records;
    }
}
